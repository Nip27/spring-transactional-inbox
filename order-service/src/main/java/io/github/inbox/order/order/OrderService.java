package io.github.inbox.order.order;

import io.github.inbox.contract.EventTypes;
import io.github.inbox.order.api.CancelOrderRequest;
import io.github.inbox.order.api.CreateOrderRequest;
import io.github.inbox.order.api.OrderResponse;
import io.github.inbox.order.events.EventPublisher;
import io.github.inbox.order.persistence.OrderEntity;
import io.github.inbox.order.persistence.OrderRepository;
import io.github.inbox.order.persistence.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final EventPublisher events;
    private final Clock clock;

    public OrderService(OrderRepository repository, OrderMapper mapper, EventPublisher events, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.events = events;
        this.clock = clock;
    }

    /**
     * Stores a new order and publishes {@link EventTypes#ORDER_CREATED} in the same transaction.
     */
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity(
                request.customerId(),
                request.customerEmail(),
                request.currency(),
                mapper.toLineEntities(request.lines()),
                clock.instant()
        );
        OrderEntity stored = repository.save(order);
        events.publish(EventTypes.ORDER_CREATED, mapper.toCreatedEvent(stored));
        return mapper.toResponse(stored);
    }

    /**
     * @throws OrderNotFoundException if there is no order with this id
     */
    @Transactional(readOnly = true)
    public OrderResponse get(Long orderId) {
        return mapper.toResponse(find(orderId));
    }

    /**
     * Cancels a created order and publishes {@link EventTypes#ORDER_CANCELLED} in the same transaction. Cancelling an already cancelled order is refused rather than ignored, so a client
     * retrying with another reason learns that the first cancellation stands.
     *
     * @throws OrderNotFoundException         if there is no order with this id
     * @throws OrderAlreadyCancelledException if the order is already cancelled
     */
    @Transactional
    public OrderResponse cancel(Long orderId, CancelOrderRequest request) {
        OrderEntity order = find(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(orderId);
        }
        order.cancel(request.reason(), clock.instant());
        events.publish(EventTypes.ORDER_CANCELLED, mapper.toCancelledEvent(order));
        return mapper.toResponse(order);
    }

    private OrderEntity find(Long orderId) {
        return repository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
