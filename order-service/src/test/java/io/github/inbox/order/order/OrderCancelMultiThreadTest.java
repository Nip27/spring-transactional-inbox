package io.github.inbox.order.order;

import io.github.inbox.contract.EventTypes;
import io.github.inbox.order.api.CancelOrderRequest;
import io.github.inbox.order.api.CreateOrderRequest;
import io.github.inbox.order.api.OrderLineRequest;
import io.github.inbox.order.persistence.OrderRepository;
import io.github.inbox.order.persistence.OrderStatus;
import io.github.inbox.order.support.ApplicationIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

/**
 * Two cancellations of one order, both past the status check before either writes - the interleaving that only a
 * version check catches.
 */
class OrderCancelMultiThreadTest extends ApplicationIntegrationTest {

    @MockitoSpyBean
    private Clock clock;

    private final OrderService tested;
    private final OrderRepository repository;
    private final JdbcTemplate jdbc;

    OrderCancelMultiThreadTest(OrderService tested, OrderRepository repository, JdbcTemplate jdbc) {
        this.tested = tested;
        this.repository = repository;
        this.jdbc = jdbc;
    }

    @AfterEach
    void tearDown() {
        reset(clock);
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("MT cancel() when two requests read the order before either cancels should let one win and refuse the other")
    void cancel_whenTwoRequestsReadBeforeEitherWrites_shouldLetOneWinAndRefuseTheOther() throws Exception {
        // given
        Long orderId = tested.create(new CreateOrderRequest(42L, "jane.doe@example.com", "EUR",
                List.of(new OrderLineRequest("sku-1", 1, BigDecimal.TEN)))).id();
        // the cancellation time is read after the status check and before the write: hold both requests there
        CountDownLatch bothChecked = new CountDownLatch(2);
        doAnswer(invocation -> {
            bothChecked.countDown();
            assertThat(bothChecked.await(15, TimeUnit.SECONDS)).as("both requests past the status check").isTrue();
            return invocation.callRealMethod();
        }).when(clock).instant();

        // when
        List<Throwable> failures = new ArrayList<>();
        int succeeded = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<?> first = pool.submit(() -> tested.cancel(orderId, new CancelOrderRequest("first")));
            Future<?> second = pool.submit(() -> tested.cancel(orderId, new CancelOrderRequest("second")));
            for (Future<?> result : List.of(first, second)) {
                try {
                    result.get(30, TimeUnit.SECONDS);
                    succeeded++;
                } catch (ExecutionException e) {
                    failures.add(e.getCause());
                }
            }
        }

        // then
        assertThat(succeeded).as("cancellations that went through").isEqualTo(1);
        assertThat(failures).as("cancellations refused").singleElement()
                .isInstanceOf(OrderAlreadyCancelledException.class);
        assertThat(repository.findById(orderId)).hasValueSatisfying(
                order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED));
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM published_events
                WHERE event_type = ? AND payload::jsonb ->> 'orderId' = ?
                """, Long.class, EventTypes.ORDER_CANCELLED, String.valueOf(orderId)))
                .as("order-cancelled events published").isEqualTo(1);
    }
}
