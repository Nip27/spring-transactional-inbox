package io.github.inbox.loyalty.api;

import io.github.inbox.loyalty.web.ProblemTypes;

import java.net.URI;

/**
 * Types of the failures of the points API; public contract like {@link ProblemTypes}.
 */
public final class PointsProblemTypes {

    public static final URI CUSTOMER_POINTS_NOT_FOUND = URI.create(ProblemTypes.BASE + "customer-points-not-found");

    private PointsProblemTypes() {
    }
}
