package io.github.inbox.order.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/**
 * Builds and stamps {@link ProblemDetail} responses. It decides no status, type or title: those come from the
 * factory of the failure being answered.
 */
public final class Problems {

    private Problems() {
    }

    public static ProblemDetail problem(HttpStatus status,
                                        URI type,
                                        String title,
                                        String detail,
                                        String instance,
                                        Instant timestamp) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(type);
        problem.setTitle(title);
        stamp(problem, instance, timestamp);
        return problem;
    }

    /**
     * Sets {@code instance} and adds the {@code path} and {@code timestamp} properties every error response carries.
     */
    public static void stamp(ProblemDetail problem, String instance, Instant timestamp) {
        problem.setInstance(URI.create(instance));
        problem.setProperty("path", instance);
        problem.setProperty("timestamp", timestamp);
    }
}
