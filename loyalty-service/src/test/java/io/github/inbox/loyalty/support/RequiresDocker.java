package io.github.inbox.loyalty.support;

import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs the annotated test class only where Docker is available, and always on CI: there a missing Docker fails the
 * build instead of skipping the tests that need it.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnabledIf("io.github.inbox.loyalty.support.Docker#availableOrCi")
public @interface RequiresDocker {
}
