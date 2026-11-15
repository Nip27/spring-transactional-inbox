package io.github.inbox.order.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The lines of an order add up to a total the order can store: at most
 * {@value OrderTotalValidator#MAX_TOTAL_TEXT}. Each line is bounded on its own, but enough of them are not, so the
 * sum is checked over the whole request and reported on {@code lines}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderTotalValidator.class)
public @interface FitsOrderTotal {

    String message() default "lines must add up to at most " + OrderTotalValidator.MAX_TOTAL_TEXT;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
