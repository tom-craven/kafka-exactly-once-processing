package com.tom.config;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka.consumer.retries")
public class KafkaRetryPreferences {
    Boolean retryByDefault;
    Map<String, Boolean> retryExceptions;
    Integer numberRetries;

    @SneakyThrows
    Map<Class<? extends Throwable>, Boolean> configureDefaultClassifier() {
        Map<Class<? extends Throwable>, Boolean> classified = new HashMap<>();
        retryExceptions.forEach((key, value) -> {
            try {
                //noinspection unchecked
                classified.put((Class<? extends Throwable>) Class.forName(key), value);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Declared class for retry classifier could not be found, does it exist in the package?");
            }
        });
        return classified;
    }
}
