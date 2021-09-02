package com.spearhead.learning.kafka.client.consumer;

import com.google.common.collect.Lists;
import com.spearhead.learning.kafka.client.utils.MyConstants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

public class TwitterTweetsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTweetsConsumer.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, MyConstants.GROUP_TO_USE);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Lists.newArrayList(MyConstants.TOPIC_TO_USE));

        ConsumerRecords<String, String> records = null;
        while (true) {
            records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                String.format("partition: %s, offset: %d, value: %s", record.partition(), record.offset(), record.value());
            });
        }

//        LOGGER.info("Number of tweets read: " + records.count());

        consumer.close();
    }
}
