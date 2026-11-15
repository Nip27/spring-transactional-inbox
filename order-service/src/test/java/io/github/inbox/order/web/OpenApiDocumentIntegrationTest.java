package io.github.inbox.order.web;

import io.github.inbox.order.support.PostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OpenApiDocumentIntegrationTest extends PostgresIntegrationTest {

    private static final String GET_ORDER = "$.paths['/api/v1/orders/{id}'].get.responses";
    private static final String CREATE_ORDER = "$.paths['/api/v1/orders'].post.responses";

    private final MockMvc mockMvc;

    OpenApiDocumentIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("IT api-docs when an endpoint answers 4xx should document the body as a problem")
    void apiDocs_whenEndpointAnswersClientError_shouldDocumentProblem() throws Exception {
        // when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(GET_ORDER + "['404'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/Problem"))
                .andExpect(jsonPath(GET_ORDER + "['404'].content", not(hasKey("*/*"))))
                .andExpect(jsonPath("$.components.schemas.Problem.properties", hasKey("errors")))
                .andExpect(jsonPath("$.components.schemas.ProblemFieldError.properties", hasKey("field")));
    }

    @Test
    @DisplayName("IT api-docs when an order is created should document 201 with the order")
    void apiDocs_whenOrderIsCreated_shouldDocumentCreatedOrder() throws Exception {
        // when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(CREATE_ORDER + "['201'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/OrderResponse"))
                .andExpect(jsonPath("$.components.schemas.OrderStatus.enum[0]").value("CREATED"))
                .andExpect(jsonPath("$.components.schemas.CreateOrderRequest.required[0]").exists());
    }
}
