package com.tom.utilities;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Disabled
public class KafkaConsumerManualTest {

    @Test
    public void readMessages() {
        @Cleanup val consumer = getConsumerFactory().createConsumer("test-client");
        consumer.subscribe(Collections.singleton("output-topic"));
        val records = consumer.poll(Duration.ofSeconds(5));
        log.info("MESSAGE COUNT - {}", records.count());
        consumer.commitSync();
    }

    public DefaultKafkaConsumerFactory<byte[], byte[]> getConsumerFactory() {
        String consumerGroup = "test-group";
        val consumerProps = KafkaTestUtils.consumerProps("localhost:9092", consumerGroup, "false");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }
}

