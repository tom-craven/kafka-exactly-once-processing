package com.tom;

import com.tom.config.EmbeddedKafkaTestContext;
import com.tom.service.MessageService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Slf4j
@ActiveProfiles
public class DeadLetterQueueTest extends EmbeddedKafkaTestContext {

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

    @Test
    public void givenErroneousMessageThenMessageIsSentToDLQ() throws InterruptedException {

        @Cleanup val consumer = getConsumerFactory().createConsumer();

        consumer.subscribe(Collections.singleton(DLQ_TOPIC));

        try {
            testProducer.sendDefault("ERRONEOUS".getBytes(StandardCharsets.UTF_8)).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(3000);

        val consumerRecords = consumer.poll(Duration.ofSeconds(5));
        consumer.commitSync();

        log.info("[MESSAGES RECEIVED] Count {}", consumerRecords.count());
        Assert.isTrue(consumerRecords.count() == 1, "all messages should be received");

    }
}
