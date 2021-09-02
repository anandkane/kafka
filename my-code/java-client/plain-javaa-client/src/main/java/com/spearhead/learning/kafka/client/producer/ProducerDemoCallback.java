package com.spearhead.learning.kafka.client.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class ProducerDemoCallback {
    private static Logger logger = LoggerFactory.getLogger(ProducerDemoCallback.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // set properties
        Properties properties = new Properties();
        properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // send message
        for (int i = 0; i < 10; i++) {
            String key = "id_" + i;
            String value = "sample message " + i;
            ProducerRecord<String, String> record = new ProducerRecord<>("topic1", key, value);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info(String.format("Topic: %s, Partition: %s, Offset: %d",
                            metadata.topic(), metadata.partition(), metadata.offset()));
                } else {
                    logger.error("Error while sending message", exception);
                }
            });
        }

        // close producer
        producer.flush();
        producer.close();
    }
}
