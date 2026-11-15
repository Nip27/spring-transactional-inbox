package io.github.inbox.order.order;

import io.github.inbox.order.api.CancelOrderRequest;
import io.github.inbox.order.api.CreateOrderRequest;
import io.github.inbox.order.api.OrderLineRequest;
import io.github.inbox.order.api.OrderResponse;
import io.github.inbox.order.persistence.OrderEntity;
import io.github.inbox.order.persistence.OrderLineEntity;
import io.github.inbox.order.persistence.OrderRepository;
import io.github.inbox.order.persistence.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

    @Mock
    private OrderRepository repository;

    private OrderService tested;

    @BeforeEach
    void setUp() {
        tested = new OrderService(repository, new OrderMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("UT create() when request is valid should store a created order stamped with the current time")
    void create_whenRequestIsValid_shouldStoreCreatedOrder() {
        // given
        when(repository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CreateOrderRequest request = new CreateOrderRequest(42L, "customer@example.com", "EUR",
                List.of(new OrderLineRequest("sku-1", 2, new BigDecimal("10.50"))));

        // when
        OrderResponse response = tested.create(request);

        // then
        ArgumentCaptor<OrderEntity> stored = ArgumentCaptor.forClass(OrderEntity.class);
        verify(repository).save(stored.capture());
        assertThat(stored.getValue().getCreatedAt()).isEqualTo(NOW);
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.totalAmount()).isEqualByComparingTo("21.00");
        assertThat(response.lines()).singleElement().satisfies(line -> assertThat(line.productId()).isEqualTo("sku-1"));
    }

    @Test
    @DisplayName("UT get() when order does not exist should throw OrderNotFoundException")
    void get_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {
        // given
        when(repository.findById(7L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> tested.get(7L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order 7 not found");
    }

    @Test
    @DisplayName("UT get() when order exists should return it")
    void get_whenOrderExists_shouldReturnIt() {
        // given
        when(repository.findById(7L)).thenReturn(Optional.of(newOrder()));

        // when
        OrderResponse response = tested.get(7L);

        // then
        assertThat(response.customerId()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("UT cancel() when order is created should cancel it with the reason and the current time")
    void cancel_whenOrderIsCreated_shouldCancelIt() {
        // given
        when(repository.findById(7L)).thenReturn(Optional.of(newOrder()));

        // when
        OrderResponse response = tested.cancel(7L, new CancelOrderRequest("changed my mind"));

        // then
        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.cancelReason()).isEqualTo("changed my mind");
        assertThat(response.cancelledAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("UT cancel() when order is already cancelled should throw OrderAlreadyCancelledException")
    void cancel_whenOrderIsAlreadyCancelled_shouldThrowOrderAlreadyCancelledException() {
        // given
        OrderEntity order = newOrder();
        order.cancel("first", NOW.minusSeconds(60));
        when(repository.findById(7L)).thenReturn(Optional.of(order));

        // when / then
        assertThatThrownBy(() -> tested.cancel(7L, new CancelOrderRequest("second")))
                .isInstanceOf(OrderAlreadyCancelledException.class)
                .hasMessage("Order 7 is already cancelled");
        assertThat(order.getCancelReason()).isEqualTo("first");
    }

    @Test
    @DisplayName("UT cancel() when order does not exist should throw OrderNotFoundException")
    void cancel_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {
        // given
        when(repository.findById(7L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> tested.cancel(7L, new CancelOrderRequest(null)))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private static OrderEntity newOrder() {
        return new OrderEntity(42L, "customer@example.com", "EUR",
                List.of(new OrderLineEntity("sku-1", 2, new BigDecimal("10.50"))), NOW.minusSeconds(3600));
    }
}
