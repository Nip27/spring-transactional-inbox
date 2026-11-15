package io.github.inbox.loyalty.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemUnitTest {

    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    @DisplayName("UT Problem when a validation problem is serialised should document exactly its keys")
    void problem_whenValidationProblemIsSerialised_shouldMatchRecordComponents() {
        // given
        ProblemDetail problem = CommonProblems.validationFailed(
                List.of(new CommonProblems.FieldError("quantity", "quantity must be positive")),
                "/api/v1/orders",
                Instant.parse("2026-11-14T10:00:00Z")
        );

        // when
        JsonNode json = mapper.valueToTree(problem);

        // then
        assertThat(keys(json)).isEqualTo(components(Problem.class));
        assertThat(keys(json.get("errors").get(0))).isEqualTo(components(Problem.ProblemFieldError.class));
    }

    private static Set<String> keys(JsonNode node) {
        Set<String> keys = new HashSet<>();
        node.fieldNames().forEachRemaining(keys::add);
        return keys;
    }

    private static Set<String> components(Class<? extends Record> type) {
        Set<String> names = new HashSet<>();
        Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).forEach(names::add);
        return names;
    }
}
