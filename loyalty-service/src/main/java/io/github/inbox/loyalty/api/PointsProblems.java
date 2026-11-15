package io.github.inbox.loyalty.api;

import io.github.inbox.loyalty.web.Problems;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.time.Instant;

final class PointsProblems {

    private PointsProblems() {
    }

    static ProblemDetail customerPointsNotFound(String detail, String instance, Instant timestamp) {
        return Problems.problem(HttpStatus.NOT_FOUND, PointsProblemTypes.CUSTOMER_POINTS_NOT_FOUND,
                "Customer points not found", detail, instance, timestamp);
    }
}
