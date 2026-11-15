package io.github.inbox.loyalty.web;

import io.github.inbox.loyalty.support.ApplicationIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
// a second context with live listeners would take events from the consumer group the flow tests rely on
@TestPropertySource(properties = "spring.kafka.listener.auto-startup=false")
class OpenApiDocumentIntegrationTest extends ApplicationIntegrationTest {

    private static final String GET_BALANCE = "$.paths['/api/v1/customers/{customerId}/points'].get.responses";

    private final MockMvc mockMvc;

    OpenApiDocumentIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("IT api-docs when the balance is not found should document the body as a problem")
    void apiDocs_whenBalanceIsNotFound_shouldDocumentProblem() throws Exception {
        // when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(GET_BALANCE + "['404'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/Problem"))
                .andExpect(jsonPath(GET_BALANCE + "['200'].content['*/*'].schema.$ref")
                        .value("#/components/schemas/PointsBalanceResponse"));
    }
}
