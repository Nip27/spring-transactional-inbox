package io.github.inbox.order.api;

import io.github.inbox.order.order.OrderAlreadyCancelledException;
import io.github.inbox.order.order.OrderNotFoundException;
import io.github.inbox.order.order.OrderService;
import io.github.inbox.order.persistence.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderControllerIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");
    private static final String VALID_ORDER = """
            {"customerId": 42, "customerEmail": "customer@example.com", "currency": "EUR",
             "lines": [{"productId": "sku-1", "quantity": 2, "unitPrice": 10.50}]}
            """;

    @MockitoBean
    private OrderService service;

    private final MockMvc mockMvc;

    OrderControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("IT create() when request is valid should answer 201 with the order and its Location")
    void create_whenRequestIsValid_shouldAnswerCreatedWithLocation() throws Exception {
        // given
        when(service.create(any(CreateOrderRequest.class))).thenReturn(order(7L, OrderStatus.CREATED));

        // when / then
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/orders/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("IT create() when fields are invalid should answer 400 naming every invalid field")
    void create_whenFieldsAreInvalid_shouldAnswerValidationProblem() throws Exception {
        // given
        String invalid = """
                {"customerId": -1, "customerEmail": "not-an-email", "currency": "eur", "lines": []}
                """;

        // when / then
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(
                        "https://github.com/dmitriy-iliyov/spring-transactional-inbox/errors/validation-failed"))
                .andExpect(jsonPath("$.errors", hasSize(4)))
                .andExpect(jsonPath("$.errors[*].field", hasItem("customerEmail")))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.timestamp").value("2026-11-14T10:00:00Z"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT create() when a line is invalid should name the field inside the line")
    void create_whenLineIsInvalid_shouldNameNestedField() throws Exception {
        // given
        String invalid = """
                {"customerId": 42, "customerEmail": "customer@example.com", "currency": "EUR",
                 "lines": [{"productId": "sku-1", "quantity": 0, "unitPrice": 10.50}]}
                """;

        // when / then
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines[0].quantity"))
                .andExpect(jsonPath("$.errors[0].message").value("quantity must be positive"));
    }

    @Test
    @DisplayName("IT create() when the lines add up to more than an order can store should answer 400 on lines")
    void create_whenTotalOverflows_shouldAnswerValidationProblem() throws Exception {
        // given
        String tooExpensive = """
                {"customerId": 42, "customerEmail": "customer@example.com", "currency": "EUR",
                 "lines": [{"productId": "sku-1", "quantity": 2, "unitPrice": 99999999999999999.99}]}
                """;

        // when / then
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(tooExpensive))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines"))
                .andExpect(jsonPath("$.errors[0].message").value("lines must add up to at most 99999999999999999.99"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT create() when a line orders more than the largest quantity should answer 400 on it")
    void create_whenQuantityIsTooLarge_shouldAnswerValidationProblem() throws Exception {
        // given
        String tooMany = """
                {"customerId": 42, "customerEmail": "customer@example.com", "currency": "EUR",
                 "lines": [{"productId": "sku-1", "quantity": 10001, "unitPrice": 1.00}]}
                """;

        // when / then
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(tooMany))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("lines[0].quantity"))
                .andExpect(jsonPath("$.errors[0].message").value("quantity must be at most 10000"));
    }

    @Test
    @DisplayName("IT create() when body is not JSON should answer 400 without reaching the service")
    void create_whenBodyIsUnreadable_shouldAnswerBadRequest() throws Exception {
        // when / then
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT get() when order exists should answer 200 with it")
    void get_whenOrderExists_shouldAnswerOk() throws Exception {
        // given
        when(service.get(7L)).thenReturn(order(7L, OrderStatus.CREATED));

        // when / then
        mockMvc.perform(get("/api/v1/orders/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].productId").value("sku-1"));
    }

    @Test
    @DisplayName("IT get() when order does not exist should answer 404 order-not-found")
    void get_whenOrderDoesNotExist_shouldAnswerNotFound() throws Exception {
        // given
        when(service.get(7L)).thenThrow(new OrderNotFoundException(7L));

        // when / then
        mockMvc.perform(get("/api/v1/orders/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value(OrderProblemTypes.ORDER_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.detail").value("Order 7 not found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/orders/7"));
    }

    @Test
    @DisplayName("IT get() when id is not positive should answer 400 naming the id")
    void get_whenIdIsNotPositive_shouldAnswerValidationProblem() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/orders/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("id"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT get() when id is not a number should answer 400")
    void get_whenIdIsNotANumber_shouldAnswerBadRequest() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/orders/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT cancel() when order is already cancelled should answer 409 order-already-cancelled")
    void cancel_whenOrderIsAlreadyCancelled_shouldAnswerConflict() throws Exception {
        // given
        when(service.cancel(eq(7L), any(CancelOrderRequest.class))).thenThrow(new OrderAlreadyCancelledException(7L));

        // when / then
        mockMvc.perform(post("/api/v1/orders/7/cancel").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(OrderProblemTypes.ORDER_ALREADY_CANCELLED.toString()));
    }

    @Test
    @DisplayName("IT cancel() when the reason is too long should name the reason field, not the request parameter")
    void cancel_whenReasonIsTooLong_shouldNameReasonField() throws Exception {
        // given
        String body = "{\"reason\": \"%s\"}".formatted("x".repeat(501));

        // when / then
        mockMvc.perform(post("/api/v1/orders/7/cancel").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("reason"))
                .andExpect(jsonPath("$.errors[0].message").value("reason must be at most 500 characters"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("IT cancel() when order is created should answer 200 with the cancelled order")
    void cancel_whenOrderIsCreated_shouldAnswerOk() throws Exception {
        // given
        when(service.cancel(eq(7L), any(CancelOrderRequest.class))).thenReturn(order(7L, OrderStatus.CANCELLED));

        // when / then
        mockMvc.perform(post("/api/v1/orders/7/cancel").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"changed my mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("IT get() when the service fails unexpectedly should answer 500 without leaking the cause")
    void get_whenServiceFailsUnexpectedly_shouldAnswerInternalError() throws Exception {
        // given
        when(service.get(7L)).thenThrow(new IllegalStateException("relation \"orders\" does not exist"));

        // when / then
        mockMvc.perform(get("/api/v1/orders/7"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("The operation could not be completed"));
    }

    @Test
    @DisplayName("IT unknown path should answer 404 as a problem")
    void request_whenPathIsUnknown_shouldAnswerNotFoundProblem() throws Exception {
        // when / then
        mockMvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/v1/unknown"));
    }

    private static OrderResponse order(Long id, OrderStatus status) {
        return new OrderResponse(id, 42L, "customer@example.com", status, "EUR", new BigDecimal("21.00"),
                List.of(new OrderLineResponse("sku-1", 2, new BigDecimal("10.50"))), NOW, null, null);
    }

    @TestConfiguration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
