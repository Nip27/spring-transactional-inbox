package io.github.inbox.notification.mail;

import io.github.inbox.notification.api.NotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceUnitTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");
    private static final UUID EVENT_ID = UUID.fromString("0195f0a0-0000-7000-8000-000000000001");

    @Mock
    private NotificationRepository notifications;

    @InjectMocks
    private NotificationQueryService tested;

    @Test
    @DisplayName("UT findByOrder() when the order was mailed should map every notification")
    void findByOrder_whenOrderWasMailed_shouldMapNotifications() {
        // given
        when(notifications.findByOrderIdOrderBySentAt(7L)).thenReturn(List.of(
                new NotificationEntity(EVENT_ID, 7L, "jane.doe@example.com", "Your order 7 is confirmed", NOW)));

        // when
        List<NotificationResponse> result = tested.findByOrder(7L);

        // then
        assertThat(result).containsExactly(
                new NotificationResponse(EVENT_ID, 7L, "jane.doe@example.com", "Your order 7 is confirmed", NOW));
    }
}
