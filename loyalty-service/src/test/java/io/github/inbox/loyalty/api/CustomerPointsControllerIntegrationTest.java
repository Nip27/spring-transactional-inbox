package io.github.inbox.loyalty.api;

import io.github.inbox.loyalty.points.CustomerPointsNotFoundException;
import io.github.inbox.loyalty.points.PointsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerPointsController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CustomerPointsControllerIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

    @MockitoBean
    private PointsService service;

    private final MockMvc mockMvc;

    CustomerPointsControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("IT balance() when the customer has points should answer 200 with the balance")
    void balance_whenCustomerHasPoints_shouldAnswerOk() throws Exception {
        // given
        when(service.balance(42L)).thenReturn(new PointsBalanceResponse(42L, 120, NOW));

        // when / then
        mockMvc.perform(get("/api/v1/customers/42/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(120))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-14T10:00:00Z"));
    }

    @Test
    @DisplayName("IT balance() when the customer has no points yet should answer 404 customer-points-not-found")
    void balance_whenCustomerHasNoPoints_shouldAnswerNotFound() throws Exception {
        // given
        when(service.balance(42L)).thenThrow(new CustomerPointsNotFoundException(42L));

        // when / then
        mockMvc.perform(get("/api/v1/customers/42/points"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(PointsProblemTypes.CUSTOMER_POINTS_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.detail").value("Customer 42 has no points yet"))
                .andExpect(jsonPath("$.timestamp").value("2026-11-14T10:00:00Z"));
    }

    @Test
    @DisplayName("IT balance() when the customer id is not positive should answer 400 naming it")
    void balance_whenCustomerIdIsNotPositive_shouldAnswerValidationProblem() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/customers/0/points"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("customerId"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT balance() when the service fails unexpectedly should answer 500 without leaking the cause")
    void balance_whenServiceFailsUnexpectedly_shouldAnswerInternalError() throws Exception {
        // given
        when(service.balance(42L)).thenThrow(new IllegalStateException("connection refused"));

        // when / then
        mockMvc.perform(get("/api/v1/customers/42/points"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("The operation could not be completed"));
    }

    @TestConfiguration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
