package io.github.inbox.loyalty.config;

import io.github.inbox.loyalty.events.UnsupportedEventException;
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
 * Every redelivery is safe: the inbox rolls its record back together with the failed handling, so a retried event
 * is handled again and a handled one is skipped. An optimistic locking failure on a balance is retried the same way,
 * on fresh state.
 */
@Configuration(proxyBeanMethods = false)
public class KafkaErrorHandlingConfiguration {

    public static final String DLT_SUFFIX = ".DLT";

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
                                                 @Value("${loyalty.kafka.max-retries:5}") int maxRetries,
                                                 @Value("${loyalty.kafka.initial-backoff:500ms}") Duration initialBackoff,
                                                 @Value("${loyalty.kafka.max-backoff:10s}") Duration maxBackoff) {
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
