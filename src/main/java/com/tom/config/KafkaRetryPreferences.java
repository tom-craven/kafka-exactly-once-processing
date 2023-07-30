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
        for (Map.Entry<String, Boolean> entry : retryExceptions.entrySet()) {
            String s = entry.getKey();
            Boolean aBoolean = entry.getValue();
            //noinspection unchecked
            classified.put((Class<? extends Throwable>) Class.forName(s), aBoolean);
        }
        return classified;
    }
}
