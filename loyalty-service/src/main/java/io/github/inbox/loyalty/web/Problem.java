package io.github.inbox.loyalty.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * The shape of every error response, for the OpenAPI document only. springdoc does not see the Jackson mixin that
 * writes the properties of a {@link org.springframework.http.ProblemDetail} at the top level, so the document
 * describes the body through this record instead.
 */
@Schema(name = "Problem", description = "Error response as defined by RFC 9457")
public record Problem(
        @Schema(description = "URI naming the kind of failure, the field to branch on",
                example = "https://github.com/dmitriy-iliyov/spring-transactional-inbox/errors/customer-points-not-found",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI type,

        @Schema(description = "Short summary of the kind of failure", example = "Customer points not found",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "HTTP status code", example = "404", requiredMode = Schema.RequiredMode.REQUIRED)
        int status,

        @Schema(description = "What happened in this occurrence", example = "Customer 42 has no points yet",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String detail,

        @Schema(description = "Path of the request that failed", example = "/api/v1/customers/42/points",
                requiredMode = Schema.RequiredMode.REQUIRED)
        URI instance,

        @Schema(description = "Path of the request that failed, repeated for clients of the Spring Boot error body",
                example = "/api/v1/customers/42/points", requiredMode = Schema.RequiredMode.REQUIRED)
        String path,

        @Schema(description = "When the failure was answered", example = "2026-11-14T10:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Instant timestamp,

        @Schema(description = "Invalid fields, present only when the request failed validation",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<ProblemFieldError> errors
) {

    @Schema(name = "ProblemFieldError", description = "One invalid field of a request")
    public record ProblemFieldError(
            @Schema(description = "Name of the field in the JSON body or of the parameter in the URL",
                    example = "customerId", requiredMode = Schema.RequiredMode.REQUIRED)
            String field,

            @Schema(description = "What is wrong with the field", example = "customerId must be positive",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String message
    ) {
    }
}
