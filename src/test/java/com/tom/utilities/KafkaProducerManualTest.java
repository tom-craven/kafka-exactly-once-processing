package com.tom.utilities;

import com.tom.config.TestConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@Slf4j
@Disabled
@SpringBootTest(classes = {TestConfig.class})
public class KafkaProducerManualTest {

    @Autowired
    byte[] fooMessage;

    @Test
    @SneakyThrows
    public void sendMessage() {
        val kt = new KafkaTemplate<>(getProducerFactory());
        kt.setDefaultTopic("input-topic");
        val messageSent = kt.sendDefault(fooMessage).get();
        kt.destroy();
    }

    public DefaultKafkaProducerFactory<byte[], byte[]> getProducerFactory() {
        val producerProps = KafkaTestUtils.producerProps("localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        return new DefaultKafkaProducerFactory<>(producerProps);
    }
}

