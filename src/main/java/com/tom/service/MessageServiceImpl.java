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

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    private final Validator validator;
    final ObjectMapper objectMapper;

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
                .map(foo1 -> Bar.builder().name(foo1.getName()).build());
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
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Foo> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }
        return foo;
    }
}
