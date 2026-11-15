package io.github.inbox.order.web;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Documents every {@code 4xx} response as a {@link Problem} in {@code application/problem+json}, replacing the
 * success body springdoc infers from the return type of the method.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    private static final String PROBLEM_JSON = org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
    private static final String PROBLEM_REF = "#/components/schemas/Problem";

    @Bean
    public OperationCustomizer problemResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getResponses() != null) {
                operation.getResponses().forEach((code, response) -> {
                    if (code.startsWith("4")) {
                        documentAsProblem(response);
                    }
                });
            }
            return operation;
        };
    }

    @Bean
    public OpenApiCustomizer problemSchemaCustomizer() {
        return openApi -> {
            Map<String, Schema> schemas = ModelConverters.getInstance(true).readAll(Problem.class);
            schemas.forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));
        };
    }

    private static void documentAsProblem(ApiResponse response) {
        response.setContent(new Content().addMediaType(
                PROBLEM_JSON,
                new MediaType().schema(new Schema<>().$ref(PROBLEM_REF))
        ));
    }
}
