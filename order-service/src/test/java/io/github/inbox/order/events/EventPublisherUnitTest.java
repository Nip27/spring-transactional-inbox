package io.github.inbox.order.events;

import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxEvent;
import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxManager;
import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxPropertiesHolder;
import io.github.dmitriyiliyov.oncebox.core.outbox.OutboxSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventPublisherUnitTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");
    private static final UUID EVENT_ID = UUID.fromString("0195f0a0-0000-7000-8000-000000000001");

    @Mock
    private OutboxPropertiesHolder outboxProperties;

    @Mock
    private OutboxSerializer serializer;

    @Mock
    private OutboxManager outbox;

    @Mock
    private PublishedEventRepository publishedEvents;

    private EventPublisher tested;

    @BeforeEach
    void setUp() {
        tested = new EventPublisher(outboxProperties, serializer, outbox, publishedEvents,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("UT publish() when event type is configured should save it to the outbox and log it under the same id")
    void publish_whenEventTypeIsConfigured_shouldSaveToOutboxAndLogUnderSameId() {
        // given
        OutboxEvent event = new OutboxEvent(EVENT_ID, "order-created", "java.lang.String", "\"payload\"");
        when(outboxProperties.existEventType("order-created")).thenReturn(true);
        when(serializer.serialize("order-created", "payload")).thenReturn(event);

        // when
        UUID id = tested.publish("order-created", "payload");

        // then
        assertThat(id).isEqualTo(EVENT_ID);
        verify(outbox).save(event);
        ArgumentCaptor<PublishedEventEntity> logged = ArgumentCaptor.forClass(PublishedEventEntity.class);
        verify(publishedEvents).save(logged.capture());
        assertThat(logged.getValue().getId()).isEqualTo(EVENT_ID);
        assertThat(logged.getValue().getEventType()).isEqualTo("order-created");
        assertThat(logged.getValue().getPayloadType()).isEqualTo("java.lang.String");
        assertThat(logged.getValue().getPayload()).isEqualTo("\"payload\"");
        assertThat(logged.getValue().getPublishedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("UT publish() when event type is not configured should refuse without touching the outbox")
    void publish_whenEventTypeIsNotConfigured_shouldThrowIllegalArgumentException() {
        // given
        when(outboxProperties.existEventType("order-shipped")).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> tested.publish("order-shipped", "payload"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown outbox event type 'order-shipped'");
        verifyNoInteractions(serializer, outbox, publishedEvents);
    }
}
