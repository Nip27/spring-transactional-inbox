package io.github.inbox.loyalty.api;

import io.github.inbox.loyalty.points.CustomerPointsNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice(basePackageClasses = CustomerPointsController.class)
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class PointsExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PointsExceptionHandler.class);

    private final Clock clock;

    public PointsExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(CustomerPointsNotFoundException.class)
    public ProblemDetail handleCustomerPointsNotFoundException(CustomerPointsNotFoundException exception,
                                                               HttpServletRequest request) {
        log.debug("{} on {}", exception.getClass().getSimpleName(), request.getRequestURI());
        return PointsProblems.customerPointsNotFound(exception.getMessage(), request.getRequestURI(), clock.instant());
    }
}
