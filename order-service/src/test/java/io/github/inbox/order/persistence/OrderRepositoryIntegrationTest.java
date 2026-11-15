package io.github.inbox.order.persistence;

import io.github.inbox.order.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-14T10:00:00Z");
    private static final Instant CANCELLED_AT = Instant.parse("2026-11-14T11:00:00Z");

    private final OrderRepository tested;
    private final TestEntityManager entityManager;

    OrderRepositoryIntegrationTest(OrderRepository tested, TestEntityManager entityManager) {
        this.tested = tested;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void cleanUp() {
        tested.deleteAll();
    }

    @Test
    @DisplayName("IT save() when order has lines should store them and read them back with the computed total")
    void save_whenOrderHasLines_shouldStoreLinesAndTotal() {
        // given
        OrderEntity order = newOrder();

        // when
        Long id = tested.saveAndFlush(order).getId();
        entityManager.clear();

        // then
        OrderEntity stored = tested.findById(id).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(stored.getTotalAmount()).isEqualByComparingTo("31.00");
        assertThat(stored.getLines())
                .extracting(OrderLineEntity::getProductId, OrderLineEntity::getQuantity)
                .containsExactly(
                        tuple("sku-1", 2),
                        tuple("sku-2", 1)
                );
    }

    @Test
    @DisplayName("IT save() when order is new should leave the cancellation columns null")
    void save_whenOrderIsNew_shouldLeaveCancellationColumnsNull() {
        // when
        Long id = tested.saveAndFlush(newOrder()).getId();
        entityManager.clear();

        // then
        OrderEntity stored = tested.findById(id).orElseThrow();
        assertThat(stored.getCancelledAt()).isNull();
        assertThat(stored.getCancelReason()).isNull();
    }

    @Test
    @DisplayName("IT save() when order is cancelled should store the cancellation and raise the version")
    void save_whenOrderIsCancelled_shouldStoreCancellationAndRaiseVersion() {
        // given
        OrderEntity order = tested.saveAndFlush(newOrder());
        long createdVersion = order.getVersion();

        // when
        order.cancel("changed my mind", CANCELLED_AT);
        tested.saveAndFlush(order);
        entityManager.clear();

        // then
        OrderEntity stored = tested.findById(order.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(stored.getCancelReason()).isEqualTo("changed my mind");
        assertThat(stored.getCancelledAt()).isEqualTo(CANCELLED_AT);
        assertThat(stored.getVersion()).isGreaterThan(createdVersion);
    }

    private static OrderEntity newOrder() {
        return new OrderEntity(
                42L,
                "customer@example.com",
                "EUR",
                List.of(
                        new OrderLineEntity("sku-1", 2, new BigDecimal("10.50")),
                        new OrderLineEntity("sku-2", 1, new BigDecimal("10.00"))
                ),
                CREATED_AT
        );
    }
}
