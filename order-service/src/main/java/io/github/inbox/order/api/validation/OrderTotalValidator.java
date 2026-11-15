package io.github.inbox.order.api.validation;

import io.github.inbox.order.api.CreateOrderRequest;
import io.github.inbox.order.api.OrderLineRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Checks {@link FitsOrderTotal}. A request whose lines are missing or incomplete passes here: the constraints on
 * the lines themselves report it.
 */
public class OrderTotalValidator implements ConstraintValidator<FitsOrderTotal, CreateOrderRequest> {

    /**
     * The largest amount the {@code NUMERIC(19, 2)} total column holds.
     */
    public static final String MAX_TOTAL_TEXT = "99999999999999999.99";

    private static final BigDecimal MAX_TOTAL = new BigDecimal(MAX_TOTAL_TEXT);

    @Override
    public boolean isValid(CreateOrderRequest request, ConstraintValidatorContext context) {
        if (request == null || request.lines() == null) {
            return true;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest line : request.lines()) {
            if (line == null || line.unitPrice() == null) {
                return true;
            }
            total = total.add(line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        if (total.compareTo(MAX_TOTAL) <= 0) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("lines")
                .addConstraintViolation();
        return false;
    }
}
