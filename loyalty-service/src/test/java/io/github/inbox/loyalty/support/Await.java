package io.github.inbox.loyalty.support;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;

/**
 * The waits of this module's tests, with their timeouts defined once.
 */
public final class Await {

    private static final Duration AT_MOST = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

    private Await() {
    }

    public static ConditionFactory awaitAtMost() {
        return Awaitility.await().atMost(AT_MOST).pollInterval(POLL_INTERVAL);
    }
}
