package io.github.inbox.notification.web;

import java.net.URI;

/**
 * Types of the failures every endpoint can answer with. A type is the field a client branches on, so a constant
 * here is public contract and is never renamed.
 */
public final class ProblemTypes {

    public static final String BASE = "https://github.com/dmitriy-iliyov/spring-transactional-inbox/errors/";

    public static final URI VALIDATION_FAILED = URI.create(BASE + "validation-failed");
    public static final URI INTERNAL_ERROR = URI.create(BASE + "internal-error");

    private ProblemTypes() {
    }
}
