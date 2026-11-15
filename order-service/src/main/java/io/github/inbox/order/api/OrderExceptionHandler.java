package io.github.inbox.order.api;

import io.github.inbox.order.order.OrderAlreadyCancelledException;
import io.github.inbox.order.order.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice(basePackageClasses = OrderController.class)
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class OrderExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderExceptionHandler.class);

    private final Clock clock;

    public OrderExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFoundException(OrderNotFoundException exception, HttpServletRequest request) {
        log.debug("{} on {}", exception.getClass().getSimpleName(), request.getRequestURI());
        return OrderProblems.orderNotFound(exception.getMessage(), request.getRequestURI(), clock.instant());
    }

    @ExceptionHandler(OrderAlreadyCancelledException.class)
    public ProblemDetail handleOrderAlreadyCancelledException(OrderAlreadyCancelledException exception,
                                                              HttpServletRequest request) {
        log.debug("{} on {}", exception.getClass().getSimpleName(), request.getRequestURI());
        return OrderProblems.orderAlreadyCancelled(exception.getMessage(), request.getRequestURI(), clock.instant());
    }
}
