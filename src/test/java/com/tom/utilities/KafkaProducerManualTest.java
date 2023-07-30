package com.tom.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import com.tom.config.TestConfig;

@Slf4j
@Disabled
@SpringBootTest(classes = {TestConfig.class})
public class KafkaProducerManualTest {

    public final String bootstrapServers = "localhost:9092";
    public final String ordersTopic = "input-topic";
    KafkaTemplate<byte[], byte[]> kt;
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    byte[] fooMessage;

    @BeforeEach
    public void setup() {
        kt = new KafkaTemplate<>(getProducerFactory());
        kt.setDefaultTopic(ordersTopic);
    }

    @AfterEach
    public void cleanUp() {
        kt.destroy();
    }

    @Test
    @SneakyThrows
    public void sendMessage() {
        val messageSent = kt.sendDefault(fooMessage).get();
    }

    public DefaultKafkaProducerFactory<byte[], byte[]> getProducerFactory() {
        val producerProps = KafkaTestUtils.producerProps(bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        return new DefaultKafkaProducerFactory<>(producerProps);
    }
}

