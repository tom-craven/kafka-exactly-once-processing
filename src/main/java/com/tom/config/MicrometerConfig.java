package com.tom.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicrometerConfig {
    @Value("${spring.application.env}")
    String environment;

    @Bean
    public Counter dlqCounter(MeterRegistry registry) {
        return Counter.builder("dlq.counter")
                .description("number of times a message has been sent to the DLQ")
                .tag("environment", environment)
                .register(registry);
    }
}
