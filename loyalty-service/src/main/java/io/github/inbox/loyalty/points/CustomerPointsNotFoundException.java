package io.github.inbox.loyalty.points;

public class CustomerPointsNotFoundException extends RuntimeException {

    public CustomerPointsNotFoundException(Long customerId) {
        super("Customer %d has no points yet".formatted(customerId));
    }
}
