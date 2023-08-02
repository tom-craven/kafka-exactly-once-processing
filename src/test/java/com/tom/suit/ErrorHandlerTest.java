package com.tom.suit;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Slf4j
@ActiveProfiles("test")
public class ErrorHandlerTest extends EmbeddedKafkaTestContext {

    private static final CharSequence SEEKS_TO_CURRENT_OFFSET = "Seek to current after exception";
    private static final CharSequence LOGS_ERROR_OFFSET = "ERROR - PARTITION";

    @Autowired
    MessageService messageService;

    private KafkaTemplate<byte[], byte[]> testProducer;

    @BeforeEach
    public void setup() {
        testProducer = new KafkaTemplate<>(getProducerFactory());
        testProducer.setDefaultTopic(INPUT_TOPIC);
    }

    @AfterEach
    public void cleanUp() {
        testProducer.destroy();
    }

    @Test
    public void givenErrorHandlerWhenExecutionFailedThenSeekToCurrentOffsetAndLog() throws ExecutionException, InterruptedException, IOException {
        @Cleanup val outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        @Cleanup val consumer = getConsumerFactory().createConsumer();

        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC));

        testProducer.sendDefault("ERRONEOUS".getBytes(StandardCharsets.UTF_8)).get();

        Thread.sleep(3000);

        consumer.poll(Duration.ofSeconds(5));
        consumer.commitSync();

        Assert.isTrue(outputStream.toString().contains(SEEKS_TO_CURRENT_OFFSET), "Seeks to the current offset after exception");
        Assert.isTrue(outputStream.toString().contains(LOGS_ERROR_OFFSET), "Logs the offset of the error");
        System.setOut(new PrintStream(System.out));
    }
}
