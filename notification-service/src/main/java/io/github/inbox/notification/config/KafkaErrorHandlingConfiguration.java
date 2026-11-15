package io.github.inbox.notification.config;

import io.github.inbox.notification.events.UnsupportedEventException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.time.Duration;

/**
 * Retries a failing event with backoff and then parks it on {@code <topic>.DLT} instead of committing past it.
 * <p>
 * The inbox here records an event before handling it, so a redelivered event whose handling failed is skipped; a
 * retry only reaches what failed before the inbox recorded anything, such as reading the message.
 */
@Configuration(proxyBeanMethods = false)
public class KafkaErrorHandlingConfiguration {

    public static final String DLT_SUFFIX = ".DLT";

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
                                                 @Value("${notification.kafka.max-retries:5}") int maxRetries,
                                                 @Value("${notification.kafka.initial-backoff:500ms}") Duration initialBackoff,
                                                 @Value("${notification.kafka.max-backoff:10s}") Duration maxBackoff) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + DLT_SUFFIX, -1)
        );
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxRetries);
        backOff.setInitialInterval(initialBackoff.toMillis());
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(maxBackoff.toMillis());
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(UnsupportedEventException.class);
        return errorHandler;
    }
}
