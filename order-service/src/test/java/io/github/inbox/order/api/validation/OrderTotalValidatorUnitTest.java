package io.github.inbox.order.api.validation;

import io.github.inbox.order.api.CreateOrderRequest;
import io.github.inbox.order.api.OrderLineRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTotalValidatorUnitTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final BigDecimal MAX_UNIT_PRICE = new BigDecimal("99999999999999999.99");

    private final Validator validator = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        FACTORY.close();
    }

    @Test
    @DisplayName("UT isValid() when the lines add up to exactly the largest storable total should accept the order")
    void isValid_whenTotalIsExactlyTheLargest_shouldAccept() {
        // when
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(order(line(MAX_UNIT_PRICE, 1)));

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UT isValid() when the lines add up to more than the order can store should report it on lines")
    void isValid_whenTotalOverflows_shouldReportOnLines() {
        // when
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(order(line(MAX_UNIT_PRICE, 2)));

        // then
        assertThat(violations).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("lines");
            assertThat(violation.getMessage()).isEqualTo("lines must add up to at most 99999999999999999.99");
        });
    }

    @Test
    @DisplayName("UT isValid() when a line has no price should leave the report to the line's own constraint")
    void isValid_whenLineHasNoPrice_shouldNotReportTotal() {
        // when
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(order(line(null, 1)));

        // then
        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("lines[0].unitPrice");
    }

    @Test
    @DisplayName("UT isValid() when a line is null should leave the report to the lines' own constraint")
    void isValid_whenLineIsNull_shouldNotReportTotal() {
        // when
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(
                new CreateOrderRequest(42L, "jane.doe@example.com", "EUR", Arrays.asList((OrderLineRequest) null)));

        // then
        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .doesNotContain("lines");
    }

    @Test
    @DisplayName("UT isValid() when there are no lines should leave the report to the lines' own constraint")
    void isValid_whenLinesAreNull_shouldNotReportTotal() {
        // when
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(
                new CreateOrderRequest(42L, "jane.doe@example.com", "EUR", null));

        // then
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactly("lines must not be null");
    }

    private static CreateOrderRequest order(OrderLineRequest line) {
        return new CreateOrderRequest(42L, "jane.doe@example.com", "EUR", List.of(line));
    }

    private static OrderLineRequest line(BigDecimal unitPrice, int quantity) {
        return new OrderLineRequest("sku-1", quantity, unitPrice);
    }
}
