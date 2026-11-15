package io.github.inbox.notification.mail;

import io.github.inbox.notification.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestConstructor;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class NotificationRepositoryIntegrationTest extends PostgresIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");
    private static final UUID FIRST_EVENT = UUID.fromString("0195f0a0-0000-7000-8000-000000000001");
    private static final UUID SECOND_EVENT = UUID.fromString("0195f0a0-0000-7000-8000-000000000002");

    private final NotificationRepository tested;

    NotificationRepositoryIntegrationTest(NotificationRepository tested) {
        this.tested = tested;
    }

    @BeforeEach
    void cleanUp() {
        tested.deleteAll();
    }

    @Test
    @DisplayName("IT findByOrderIdOrderBySentAt() when an order has several notifications should return them oldest first")
    void findByOrderIdOrderBySentAt_whenSeveralNotifications_shouldReturnOldestFirst() {
        // given
        tested.save(new NotificationEntity(SECOND_EVENT, 7L, "jane.doe@example.com", "second", NOW.plusSeconds(60)));
        tested.save(new NotificationEntity(FIRST_EVENT, 7L, "jane.doe@example.com", "first", NOW));
        tested.save(new NotificationEntity(UUID.randomUUID(), 8L, "john.doe@example.com", "other order", NOW));

        // when / then
        assertThat(tested.findByOrderIdOrderBySentAt(7L))
                .extracting(NotificationEntity::getEventId, NotificationEntity::getSubject)
                .containsExactly(
                        tuple(FIRST_EVENT, "first"),
                        tuple(SECOND_EVENT, "second")
                );
    }

    @Test
    @DisplayName("IT save() when the event already has a notification should reject the second one")
    void save_whenEventAlreadyHasNotification_shouldRejectSecond() {
        // given
        tested.saveAndFlush(new NotificationEntity(FIRST_EVENT, 7L, "jane.doe@example.com", "first", NOW));

        // when / then
        assertThatThrownBy(() -> tested.saveAndFlush(
                new NotificationEntity(FIRST_EVENT, 7L, "jane.doe@example.com", "again", NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("IT existsByEventId() when only another event was mailed should answer false")
    void existsByEventId_whenOnlyAnotherEventWasMailed_shouldAnswerFalse() {
        // given
        tested.saveAndFlush(new NotificationEntity(FIRST_EVENT, 7L, "jane.doe@example.com", "first", NOW));

        // when / then
        assertThat(tested.existsByEventId(FIRST_EVENT)).isTrue();
        assertThat(tested.existsByEventId(SECOND_EVENT)).isFalse();
    }

    @Test
    @DisplayName("IT findByOrderIdOrderBySentAt() when the order has no notification should return an empty list")
    void findByOrderIdOrderBySentAt_whenNoNotification_shouldReturnEmptyList() {
        // when / then
        assertThat(tested.findByOrderIdOrderBySentAt(7L)).isEmpty();
    }
}
