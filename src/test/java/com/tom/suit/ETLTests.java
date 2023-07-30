package com.tom.suit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tom.config.EmbeddedKafkaTestContext;
import com.tom.model.Bar;
import com.tom.service.MessageService;
import jakarta.validation.Valid;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Slf4j
@ActiveProfiles("test")
public class ETLTests extends EmbeddedKafkaTestContext {

    @Autowired
    byte[] fooMessage;
    @Autowired
    byte[] invalidFooMessage;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MessageService messageService;

    @Autowired
    KafkaTemplate<byte[], byte[]> kt;

    private KafkaTemplate<byte[], byte[]> testProducer;

    @BeforeEach
    public void setup() {
        testProducer = new KafkaTemplate<>(getProducerFactory());
        testProducer.setDefaultTopic(INPUT_TOPIC);
    }

    @AfterEach
    public void cleanUp() {
        kt.destroy();
    }

    @SneakyThrows
    @Test
    public void givenInvalidFooWhenMessageIsSentThenExceptionIsThrown() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        @Cleanup val consumer = getConsumerFactory().createConsumer();
        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC));

        try {
            testProducer.sendDefault(invalidFooMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(3000);

        consumer.poll(Duration.ofSeconds(5));
        consumer.commitSync();

        Assert.isTrue(outputStream.toString().contains("FooDeserializationException"), "The Foo should be invalid");
        outputStream.close();
        System.setOut(new PrintStream(System.out));
    }

    @SneakyThrows
    @Test
    public void givenFooWhenMessageIsSentThenTheMessageIsTransformed() {

        @Cleanup val consumer = getConsumerFactory().createConsumer();
        consumer.subscribe(Collections.singleton(INPUT_TOPIC));
        consumer.poll(Duration.ofSeconds(5));

        try {
            testProducer.sendDefault(fooMessage).get();
            testProducer.flush();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(3000);
        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC));
        val consumerRecords = consumer.poll(Duration.ofSeconds(5));
        consumer.commitSync();

        log.info("[MESSAGES RECEIVED] Count {}", consumerRecords.count());
        Assert.isTrue(consumerRecords.count() == 1, "all messages should be received");
        consumerRecords.forEach(this::assertTransformed);
    }

    @SneakyThrows
    private void assertTransformed(ConsumerRecord<byte[], byte[]> consumerRecord) {
        @Valid Bar order = objectMapper.readValue(consumerRecord.value(), Bar.class);
        Assert.isTrue(order.getName().equals("tom"), "the conversion should be done");
    }
}
