package io.github.inbox.notification.support;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * One container per engine for the whole test JVM. Spring caches a context across test classes, so a container
 * stopped at the end of one class would leave the next class with a context pointing at a dead port.
 */
public final class Containers {

    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6"))
                    .withDatabaseName("notifications")
                    .withUsername("inbox")
                    .withPassword("inbox");

    private Containers() {
    }

    public static void startPostgres() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }
}
