package com.tom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tom.model.Foo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@TestConfiguration
public class TestConfig {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ResourceLoader resourceLoader;

    @Bean("fooMessage")
    @SneakyThrows
    public byte[] fooExampleMessage() {
        return objectMapper.writeValueAsBytes(Foo.builder()
                .name("tom")
                .number(1)
                .build());
    }

    @Bean("invalidFooMessage")
    @SneakyThrows
    public byte[] invalidFooExampleMessage() {
        return objectMapper.writeValueAsBytes(Foo.builder().name("tom")
                .build());
    }
}
