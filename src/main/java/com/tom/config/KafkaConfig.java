package com.tom.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.event.KafkaEvent;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Slf4j
@Configuration
public class KafkaConfig implements ApplicationListener<KafkaEvent> {
    final KafkaTemplate<byte[], byte[]> template;
    final ConsumerFactory<byte[], byte[]> consumerFactory;
    final private KafkaRetryPreferences retryPreferences;

    public KafkaConfig(KafkaRetryPreferences kafkaRetryProperties, ConsumerFactory<byte[], byte[]> consumerFactory, KafkaTemplate<byte[], byte[]> template) {
        this.retryPreferences = kafkaRetryProperties;
        this.consumerFactory = consumerFactory;
        this.template = template;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<byte[], byte[]> kafkaListenerContainerFactory(
            @Autowired DefaultErrorHandler defaultErrorHandler
    ) {
        val factory = new ConcurrentKafkaListenerContainerFactory<byte[], byte[]>();
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(defaultErrorHandler);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    DefaultErrorHandler errorHandler(
            @Value("${spring.kafka.consumer.dlq-topic}") String deadLetterQueue
    ) {
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(template,
                (rec, ex) -> {
                    log.error("[DLQ RESOLVER] - Message with key {} has been sent to DLQ with exception", rec.key(), ex);
                    return new TopicPartition(deadLetterQueue, 0);
                });
        val errorhandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, new ExponentialBackOffWithMaxRetries(retryPreferences.getNumberRetries()));
        errorhandler.setClassifications(retryPreferences.configureDefaultClassifier(), retryPreferences.getRetryByDefault());
        return errorhandler;
    }

    @Bean
    @ConditionalOnProperty(value = "spring.profiles.active", havingValue = "default")
    public NewTopic inputTopic(@Value("${spring.kafka.consumer.default-topic}") String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "spring.profiles.active", havingValue = "default")
    public NewTopic dlq(@Value("${spring.kafka.consumer.dlq-topic}") String name) {
        return TopicBuilder.name(name)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "spring.profiles.active", havingValue = "default")
    public NewTopic outputTopic(@Value("${spring.kafka.template.default-topic}") String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Override
    public void onApplicationEvent(KafkaEvent event) {
        log.debug("[KAFKA - EVENT {}]", event);
    }
}
