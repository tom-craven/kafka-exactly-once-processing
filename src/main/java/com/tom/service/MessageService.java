package com.tom.service;

import com.tom.model.Bar;
import com.tom.model.Foo;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

public interface MessageService {

    default Mono<Message<byte[]>> process(byte[] payload) {
        return Mono.just(payload)
                .flatMap(this::extract)
                .flatMap(this::transfer)
                .flatMap(this::load);
    }

    Mono<? extends Message<byte[]>> load(Bar bar);

    <R> Mono<Bar> transfer(Foo foo);

    <R> Mono<Foo> extract(byte[] payload);
}
