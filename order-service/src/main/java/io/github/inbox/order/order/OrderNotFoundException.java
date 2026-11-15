package io.github.inbox.order.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order %d not found".formatted(orderId));
    }
}
