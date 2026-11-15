package io.github.inbox.order.order;

import io.github.inbox.order.api.OrderLineRequest;
import io.github.inbox.order.api.OrderLineResponse;
import io.github.inbox.order.api.OrderResponse;
import io.github.inbox.order.persistence.OrderEntity;
import io.github.inbox.order.persistence.OrderLineEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public List<OrderLineEntity> toLineEntities(List<OrderLineRequest> lines) {
        return lines.stream()
                .map(line -> new OrderLineEntity(line.productId(), line.quantity(), line.unitPrice()))
                .toList();
    }

    public OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getLines().stream()
                        .map(line -> new OrderLineResponse(line.getProductId(), line.getQuantity(), line.getUnitPrice()))
                        .toList(),
                order.getCreatedAt(),
                order.getCancelledAt(),
                order.getCancelReason()
        );
    }
}
