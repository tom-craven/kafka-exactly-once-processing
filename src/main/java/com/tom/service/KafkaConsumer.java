package com.tom.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.tom.Constants.ERROR_OFFSET;
import static com.tom.Constants.RECEIVED_PARTITION_OFFSET;

@Slf4j
@Component
@KafkaListener(
        topics = "${spring.kafka.consumer.default-topic}",
        containerFactory = "kafkaListenerContainerFactory",
        concurrency = "${spring.kafka.consumer.concurrency}")
public class KafkaConsumer {

    private final KafkaTemplate<byte[], byte[]> kt;
    private final MessageService orderService;

    public KafkaConsumer(KafkaTemplate<byte[], byte[]> kt, MessageService orderService) {
        this.kt = kt;
        this.orderService = orderService;
    }

    @KafkaHandler
    public void consumeMessage(
            @Header(value = KafkaHeaders.OFFSET, required = false) String offset,
            @Header(value = KafkaHeaders.PARTITION, required = false) String partitionID,
            Acknowledgment ack,
            @Payload byte[] payload
    ) {
        log.info(RECEIVED_PARTITION_OFFSET, partitionID, offset);
        kt.executeInTransaction(processMessage(offset, partitionID, ack, payload));
    }

    private KafkaOperations.OperationsCallback<byte[], byte[], Boolean> processMessage(String offset, String partitionID, Acknowledgment ack, byte[] payload) {
        return operations -> {
            orderService
                    .process(payload)
                    .doOnNext(kt::send)
                    .doOnSuccess(message -> {
                        ack.acknowledge();
                        log.info("[OK - Message process completed @ partition {} offset {}]", partitionID, offset);
                    })
                    .doOnError(throwable -> log.error(ERROR_OFFSET, partitionID, offset, throwable.getMessage())).block();
            return true;
        };
    }
}