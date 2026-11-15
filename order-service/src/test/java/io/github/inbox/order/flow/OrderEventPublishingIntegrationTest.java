package io.github.inbox.order.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inbox.contract.EventTypes;
import io.github.inbox.contract.Topics;
import io.github.inbox.order.events.PublishedEventRepository;
import io.github.inbox.order.support.ApplicationIntegrationTest;
import io.github.inbox.order.support.Containers;
import io.github.inbox.order.support.RecordingKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static io.github.inbox.order.support.Await.awaitAtMost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * An order placed over HTTP, through the outbox, to the record on Kafka.
 */
@AutoConfigureMockMvc
class OrderEventPublishingIntegrationTest extends ApplicationIntegrationTest {

    private static final String EVENT_ID_HEADER = "oncebox_event_id";
    private static final String EVENT_TYPE_HEADER = "oncebox_event_type";

    @MockitoSpyBean
    private PublishedEventRepository publishedEvents;

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private RecordingKafkaConsumer kafka;

    OrderEventPublishingIntegrationTest(MockMvc mockMvc, JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {
        kafka = new RecordingKafkaConsumer(Containers.KAFKA.getBootstrapServers(), Topics.ORDER_EVENTS);
    }

    @AfterEach
    void tearDown() {
        kafka.close();
        reset(publishedEvents);
    }

    @Test
    @DisplayName("IT create order when it is stored should send order-created under the id it was logged with")
    void createOrder_whenStored_shouldSendOrderCreatedUnderLoggedId() throws Exception {
        // when
        long orderId = createOrder("jane.doe@example.com");

        // then
        UUID eventId = loggedEventId(orderId, EventTypes.ORDER_CREATED);
        ConsumerRecord<String, String> record = awaitRecord(eventId);
        assertThat(RecordingKafkaConsumer.header(record, EVENT_TYPE_HEADER)).isEqualTo(EventTypes.ORDER_CREATED);
        JsonNode payload = objectMapper.readTree(record.value());
        assertThat(payload.get("orderId").asLong()).isEqualTo(orderId);
        assertThat(payload.get("customerEmail").asText()).isEqualTo("jane.doe@example.com");
        assertThat(payload.get("lines").get(0).get("productId").asText()).isEqualTo("sku-1");
        assertThat(payload.get("createdAt").isTextual()).as(record.value()).isTrue();
    }

    @Test
    @DisplayName("IT cancel order when it is created should send order-cancelled with the reason")
    void cancelOrder_whenCreated_shouldSendOrderCancelled() throws Exception {
        // given
        long orderId = createOrder("john.doe@example.com");

        // when
        mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Ordered by mistake\"}"))
                .andExpect(status().isOk());

        // then
        UUID eventId = loggedEventId(orderId, EventTypes.ORDER_CANCELLED);
        JsonNode payload = objectMapper.readTree(awaitRecord(eventId).value());
        assertThat(payload.get("reason").asText()).isEqualTo("Ordered by mistake");
    }

    @Test
    @DisplayName("IT create order when the event log cannot be written should store neither the order nor the event")
    void createOrder_whenEventLogFails_shouldRollBackOrderAndOutboxEvent() throws Exception {
        // given
        doThrow(new IllegalStateException("log unavailable")).when(publishedEvents).save(any());
        long ordersBefore = count("orders");
        long outboxBefore = count("outbox_events");

        // when
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(order("x@example.com")))
                .andExpect(status().isInternalServerError());

        // then
        assertThat(count("orders")).isEqualTo(ordersBefore);
        assertThat(count("outbox_events")).isEqualTo(outboxBefore);
    }

    private long createOrder(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private UUID loggedEventId(long orderId, String eventType) {
        return jdbc.queryForObject(
                "SELECT id FROM published_events WHERE event_type = ? AND payload::jsonb ->> 'orderId' = ?",
                UUID.class, eventType, String.valueOf(orderId));
    }

    private ConsumerRecord<String, String> awaitRecord(UUID eventId) {
        return awaitAtMost().until(() -> kafka.findByHeader(EVENT_ID_HEADER, eventId.toString()),
                Optional::isPresent).orElseThrow();
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
    }

    private static String order(String email) {
        return """
                {"customerId": 42, "customerEmail": "%s", "currency": "EUR",
                 "lines": [{"productId": "sku-1", "quantity": 2, "unitPrice": 10.50}]}
                """.formatted(email);
    }
}
