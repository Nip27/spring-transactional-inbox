package io.github.inbox.order.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.time.Instant;
import java.util.List;

final class CommonProblems {

    static final String INTERNAL_ERROR_DETAIL = "The operation could not be completed";

    private CommonProblems() {
    }

    static ProblemDetail validationFailed(List<FieldError> errors, String instance, Instant timestamp) {
        ProblemDetail problem = Problems.problem(
                HttpStatus.BAD_REQUEST,
                ProblemTypes.VALIDATION_FAILED,
                "Validation failed",
                "The request has %d invalid field(s)".formatted(errors.size()),
                instance,
                timestamp
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    static ProblemDetail internalError(String instance, Instant timestamp) {
        return Problems.problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ProblemTypes.INTERNAL_ERROR,
                "Internal error",
                INTERNAL_ERROR_DETAIL,
                instance,
                timestamp
        );
    }

    /**
     * One invalid field of a request.
     *
     * @param field   the name of the field in the JSON body or the parameter in the URL
     * @param message what is wrong with it
     */
    record FieldError(String field, String message) {
    }
}
