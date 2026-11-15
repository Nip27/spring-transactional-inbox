package io.github.inbox.notification.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Answers the framework exceptions, validation and every failure no module advice took, at the lowest precedence
 * so that a module advice ahead of it answers its own exceptions first.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception, HttpServletRequest request) {
        log.error("Request {} {} failed", request.getMethod(), request.getRequestURI(), exception);
        return CommonProblems.internalError(request.getRequestURI(), clock.instant());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<CommonProblems.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new CommonProblems.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return validationFailed(exception, errors, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException exception,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        List<CommonProblems.FieldError> errors = new ArrayList<>();
        for (ParameterValidationResult result : exception.getParameterValidationResults()) {
            String name = result.getMethodParameter().getParameterName();
            if (result instanceof ParameterErrors body) {
                // a validated body or model attribute: name the fields inside it, not the Java parameter
                body.getFieldErrors().forEach(
                        error -> errors.add(new CommonProblems.FieldError(error.getField(), error.getDefaultMessage())));
                body.getGlobalErrors().forEach(
                        error -> errors.add(new CommonProblems.FieldError(name, error.getDefaultMessage())));
            } else {
                result.getResolvableErrors()
                        .forEach(error -> errors.add(new CommonProblems.FieldError(name, error.getDefaultMessage())));
            }
        }
        return validationFailed(exception, errors, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        if (body == null && exception instanceof ErrorResponse errorResponse) {
            body = errorResponse.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        }
        if (body instanceof ProblemDetail problem && !isStamped(problem)) {
            Problems.stamp(problem, uri, clock.instant());
        }
        log.debug("Request {} answered with {}: {}", uri, statusCode.value(), exception.getClass().getSimpleName());
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }

    private static boolean isStamped(ProblemDetail problem) {
        return problem.getProperties() != null && problem.getProperties().containsKey("path");
    }

    private ResponseEntity<Object> validationFailed(Exception exception,
                                                    List<CommonProblems.FieldError> errors,
                                                    HttpHeaders headers,
                                                    WebRequest request) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        ProblemDetail problem = CommonProblems.validationFailed(errors, uri, clock.instant());
        return handleExceptionInternal(exception, problem, headers, HttpStatusCode.valueOf(problem.getStatus()), request);
    }
}
