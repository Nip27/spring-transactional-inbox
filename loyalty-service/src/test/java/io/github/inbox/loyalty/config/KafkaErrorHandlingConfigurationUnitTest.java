package io.github.inbox.loyalty.config;

import io.github.inbox.loyalty.events.UnsupportedEventException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaErrorHandlingConfigurationUnitTest {

    @SuppressWarnings("unchecked")
    private final DefaultErrorHandler tested = new KafkaErrorHandlingConfiguration().kafkaErrorHandler(
            mock(KafkaTemplate.class), 5, Duration.ofMillis(500), Duration.ofSeconds(10));

    @Test
    @DisplayName("UT kafkaErrorHandler() when an optimistic locking failure is thrown should retry the event")
    void kafkaErrorHandler_whenOptimisticLockingFails_shouldRetry() {
        // when / then
        assertThat(tested.removeClassification(ObjectOptimisticLockingFailureException.class)).isNull();
    }

    @Test
    @DisplayName("UT kafkaErrorHandler() when an event is unsupported should not retry it")
    void kafkaErrorHandler_whenEventIsUnsupported_shouldNotRetry() {
        // when / then
        assertThat(tested.removeClassification(UnsupportedEventException.class)).isFalse();
    }
}
