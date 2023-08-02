package com.tom.config;

import com.tom.MessageProcessingApplication;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;


@Slf4j
@SpringBootTest(classes = {MessageProcessingApplication.class, TestConfig.class})
@EmbeddedKafka(topics = {
        EmbeddedKafkaTestContext.INPUT_TOPIC,
        EmbeddedKafkaTestContext.OUTPUT_TOPIC,
        EmbeddedKafkaTestContext.DLQ_TOPIC},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers", partitions = 1,
        brokerProperties = {"transaction.state.log.replication.factor=1", "transaction.state.log.min.isr=1"})
public class EmbeddedKafkaTestContext {

    public static final String INPUT_TOPIC = "input-topic";

    public static final String CONSUMER_GROUP = "message-processor-test-group";
    public static final String DLQ_TOPIC = "dlq-topic";
    public static final String OUTPUT_TOPIC = "output-topic";

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    /**
     * @return Initialised Kafka Producer factory with embedded kafka broker
     */
    public DefaultKafkaProducerFactory<byte[], byte[]> getProducerFactory() {
        val producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    /**
     * @return Initialised Kafka Consumer factory with embedded kafka broker
     */

    public DefaultKafkaConsumerFactory<byte[], byte[]> getConsumerFactory() {
        val consumerProps = KafkaTestUtils.consumerProps(CONSUMER_GROUP, "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }
}
