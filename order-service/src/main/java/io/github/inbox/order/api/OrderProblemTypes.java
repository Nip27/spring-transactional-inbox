package io.github.inbox.order.api;

import io.github.inbox.order.web.ProblemTypes;

import java.net.URI;

/**
 * Types of the failures of the orders API; public contract like {@link ProblemTypes}.
 */
public final class OrderProblemTypes {

    public static final URI ORDER_NOT_FOUND = URI.create(ProblemTypes.BASE + "order-not-found");
    public static final URI ORDER_ALREADY_CANCELLED = URI.create(ProblemTypes.BASE + "order-already-cancelled");

    private OrderProblemTypes() {
    }
}
