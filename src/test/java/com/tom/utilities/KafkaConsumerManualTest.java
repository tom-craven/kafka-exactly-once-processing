package com.tom.utilities;

import java.time.Duration;
import java.util.Collections;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Disabled
public class KafkaConsumerManualTest {

	public final String bootstrapServers = "localhost:9092";
	private Consumer<byte[], byte[]> consumer;

	@BeforeEach
	public void setup() {
		consumer = getConsumerFactory().createConsumer("test-client");
	}

	@AfterEach
	public void cleanUp() {
		consumer.close();
	}

	@Test
	public void readMessages() {
		consumer.subscribe(Collections.singleton("output-topic"));

		val records = consumer.poll(Duration.ofSeconds(5));
		log.info("MESSAGE COUNT - {}", records.count());

		consumer.commitSync();
	}

	public DefaultKafkaConsumerFactory<byte[], byte[]> getConsumerFactory() {
		String consumerGroup = "test-group";
		val consumerProps = KafkaTestUtils.consumerProps(bootstrapServers, consumerGroup, "false");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return new DefaultKafkaConsumerFactory<>(consumerProps);
	}
}

