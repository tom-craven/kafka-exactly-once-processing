package com.tom;

import com.tom.config.EmbeddedKafkaTestContext;
import com.tom.service.KafkaConsumer;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@AutoConfigureObservability
@SpringBootTest(classes = {MessageProcessingApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=9999"})
@ActiveProfiles({"local", "test"})
public class PrometheusTest extends EmbeddedKafkaTestContext {
    @Autowired
    KafkaConsumer kafkaListenerHandler;
    WebTestClient webClient;
    @LocalServerPort
    int port;
    private KafkaTemplate<byte[], byte[]> testProducer;

    @BeforeEach
    void init() {
        testProducer = new KafkaTemplate<>(getProducerFactory());
        testProducer.setDefaultTopic(INPUT_TOPIC);
        webClient = WebTestClient.bindToServer().build();
    }

    @AfterEach
    public void cleanUp() {
        testProducer.destroy();
    }

    @Test
    @SneakyThrows
    public void givenPrometheusWhenDLQMessageIsSentThenCounterIsIncremented() {
        testProducer.sendDefault("BAD".getBytes(StandardCharsets.UTF_8)).get();

        Thread.sleep(4000);

        val response = webClient.get()
                .uri("http://localhost:" + port + "/actuator/metrics/dlq.counter")
                .exchange();

        response.expectStatus().is2xxSuccessful().expectBody()
                .jsonPath("$.measurements[0].value")
                .isEqualTo("1.0");
    }
}
