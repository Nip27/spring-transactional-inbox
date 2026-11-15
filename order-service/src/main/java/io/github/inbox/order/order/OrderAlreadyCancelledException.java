package io.github.inbox.order.order;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(Long orderId) {
        super("Order %d is already cancelled".formatted(orderId));
    }
}
