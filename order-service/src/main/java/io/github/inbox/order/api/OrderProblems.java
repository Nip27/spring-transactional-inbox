package io.github.inbox.order.api;

import io.github.inbox.order.web.Problems;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.time.Instant;

final class OrderProblems {

    private OrderProblems() {
    }

    static ProblemDetail orderNotFound(String detail, String instance, Instant timestamp) {
        return Problems.problem(HttpStatus.NOT_FOUND, OrderProblemTypes.ORDER_NOT_FOUND, "Order not found",
                detail, instance, timestamp);
    }

    static ProblemDetail orderAlreadyCancelled(String detail, String instance, Instant timestamp) {
        return Problems.problem(HttpStatus.CONFLICT, OrderProblemTypes.ORDER_ALREADY_CANCELLED,
                "Order already cancelled", detail, instance, timestamp);
    }
}
