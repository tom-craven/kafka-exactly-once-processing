package com.tom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tom.model.Bar;
import com.tom.model.Foo;
import com.tom.model.FooDeserializationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    final ObjectMapper objectMapper;
    private final Validator validator;

    public MessageServiceImpl(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public Mono<? extends Message<byte[]>> load(Bar bar) {
        return Mono.just(bar)
                .map(this::writeBytes)
                .map(bytes -> MessageBuilder
                        .withPayload(bytes)
                        .build());
    }

    @Override
    public <R> Mono<Bar> transfer(Foo foo) {
        return Mono.just(foo)
                .map(foo1 -> Bar
                        .builder()
                        .number(foo1.getNumber())
                        .name(foo1.getName()).build());
    }

    @Override
    @SneakyThrows
    public <R> Mono<Foo> extract(byte[] payload) {
        return Mono.just(objectMapper.readValue(payload, Foo.class))
                .map(this::validateFoo)
                .onErrorMap(FooDeserializationException::new);
    }

    @SneakyThrows
    private byte[] writeBytes(Bar bar) {
        return objectMapper.writeValueAsBytes(bar);
    }

    public Foo validateFoo(Foo foo) {
        Set<ConstraintViolation<Foo>> violations = validator.validate(foo);

        if (!violations.isEmpty()) {
            String sb = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }
        return foo;
    }
}
