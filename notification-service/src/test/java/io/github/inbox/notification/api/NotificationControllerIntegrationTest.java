package io.github.inbox.notification.api;

import io.github.inbox.notification.mail.NotificationQueryService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class NotificationControllerIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");
    private static final UUID EVENT_ID = UUID.fromString("0195f0a0-0000-7000-8000-000000000001");

    @MockitoBean
    private NotificationQueryService service;

    private final MockMvc mockMvc;

    NotificationControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("IT findByOrder() when the order was mailed should answer 200 with the notifications")
    void findByOrder_whenOrderWasMailed_shouldAnswerOk() throws Exception {
        // given
        when(service.findByOrder(7L)).thenReturn(List.of(
                new NotificationResponse(EVENT_ID, 7L, "jane.doe@example.com", "Your order 7 is confirmed", NOW)));

        // when / then
        mockMvc.perform(get("/api/v1/notifications").param("orderId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventId").value(EVENT_ID.toString()));
    }

    @Test
    @DisplayName("IT findByOrder() when nothing was mailed should answer 200 with an empty list")
    void findByOrder_whenNothingWasMailed_shouldAnswerEmptyList() throws Exception {
        // given
        when(service.findByOrder(7L)).thenReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/v1/notifications").param("orderId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("IT findByOrder() when orderId is missing should answer 400")
    void findByOrder_whenOrderIdIsMissing_shouldAnswerBadRequest() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/v1/notifications"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT findByOrder() when orderId is not positive should answer 400 naming it")
    void findByOrder_whenOrderIdIsNotPositive_shouldAnswerValidationProblem() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/notifications").param("orderId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("orderId"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT findByOrder() when the service fails unexpectedly should answer 500 without leaking the cause")
    void findByOrder_whenServiceFailsUnexpectedly_shouldAnswerInternalError() throws Exception {
        // given
        when(service.findByOrder(7L)).thenThrow(new IllegalStateException("connection refused"));

        // when / then
        mockMvc.perform(get("/api/v1/notifications").param("orderId", "7"))
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
