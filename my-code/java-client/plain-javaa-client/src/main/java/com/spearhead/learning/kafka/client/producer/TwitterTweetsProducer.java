package com.spearhead.learning.kafka.client.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.spearhead.learning.kafka.client.utils.MyConstants;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.internals.ErrorLoggingCallback;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterTweetsProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTweetsProducer.class);

    public static final String CONSUMER_KEY = "LS9O0Vi9ysnTriWBFRNjF9dzN";
    public static final String CONSUMER_SECRET = "DgRoqyOjdTmbW30VJ1sXuLohfBSB4RwGk8iiHTefgm3H3jeFDx";
    public static final String TOKEN = "201149726-OyLRNrmJ1bW75IDg6AaNHxJekXInwBLTfU1BSUy8";
    public static final String TOKEN_SECRET = "34O6iVWgtXfMr71UA5sqd0YyBJDAbpE1R42IahrJtCJW5";

    public static void main(String[] args) {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(1000);
        BasicClient client = twitterClient(queue);
        client.connect();

        KafkaProducer<String, String> producer = kafkaProducer();
        try {
            while (!client.isDone()) {
//                String tweetRaw = queue.take();
//                System.out.println("Polling....");
                String tweetRaw = queue.poll(5, TimeUnit.SECONDS);
                ObjectMapper objectMapper = new ObjectMapper();
                HashMap tweet = objectMapper.readValue(tweetRaw, HashMap.class);

                String text = tweet.get("text") != null ? tweet.get("text").toString() : "";
                ProducerRecord<String, String> record = new ProducerRecord<>(MyConstants.TOPIC_TO_USE, text);
                producer.send(record, new ErrorLoggingCallback(MyConstants.TOPIC_TO_USE, null, record.value().getBytes(), true));
                LOGGER.info(text);
//                System.out.println(tweet.get("text"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            client.stop();
            producer.close();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Stopping application...");
            LOGGER.info("Stopping twitter client...");
            client.stop();
            LOGGER.info("Closing kafka producer...");
            producer.flush();
            producer.close();
        }));
    }

    private static KafkaProducer<String, String> kafkaProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    private static BasicClient twitterClient(LinkedBlockingQueue<String> queue) {
        List<String> terms = Lists.newArrayList("bitcoin", "usa", "politics", "sport", "soccer");
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        endpoint.trackTerms(terms);

        OAuth1 oAuth1 = new OAuth1(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        BasicClient client = new ClientBuilder().hosts(Constants.STREAM_HOST).endpoint(endpoint)
                .authentication(oAuth1)
                .processor(new StringDelimitedProcessor(queue)).build();
        return client;
    }
}
