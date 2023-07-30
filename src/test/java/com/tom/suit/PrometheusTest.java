package com.tom.suit;

import com.fasterxml.jackson.databind.JsonNode;
import com.tom.MessageProcessingApplication;
import com.tom.config.EmbeddedKafkaTestContext;
import com.tom.service.KafkaConsumer;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@AutoConfigureObservability
@SpringBootTest(classes = {MessageProcessingApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=9999"})
public class PrometheusTest extends EmbeddedKafkaTestContext {
    @Autowired
    KafkaConsumer kafkaListenerHandler;

    @LocalServerPort
    int port;
    private KafkaTemplate<byte[], byte[]> testProducer;

    @BeforeEach
    void init() {
        testProducer = new KafkaTemplate<>(getProducerFactory());
        testProducer.setDefaultTopic(INPUT_TOPIC);
    }

    @AfterEach
    public void cleanUp() {
        testProducer.destroy();
    }

    @Test
    @SneakyThrows
    public void givenPrometheusWhenDLQMessageIsSentThenCounterIsIncremented() {
        @Cleanup val consumer = getConsumerFactory().createConsumer();
        val webTestClient = WebTestClient.bindToServer().build();
        val webClient = WebClient.builder().build();

        consumer.subscribe(Collections.singleton(INPUT_TOPIC));
        consumer.poll(Duration.ofSeconds(5));
        consumer.commitSync();

        val responseBefore = Objects.requireNonNull(webClient.get()
                .uri("http://localhost:" + port + "/actuator/metrics/dlq.counter").retrieve().bodyToMono(JsonNode.class).block());

        val expected = Float.parseFloat(responseBefore.get("measurements").get(0).get("value").asText())+1;

        testProducer.sendDefault("BAD".getBytes(StandardCharsets.UTF_8)).get();

        Thread.sleep(3000);

        consumer.poll(Duration.ofSeconds(5));
        consumer.commitSync();

        val response = webTestClient.get()
                .uri("http://localhost:" + port + "/actuator/metrics/dlq.counter")
                .exchange();

        response.expectStatus().is2xxSuccessful().expectBody()
                .jsonPath("$.measurements[0].value")
                .isEqualTo(String.valueOf(expected));
    }
}
