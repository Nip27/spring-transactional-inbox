package io.github.inbox.order.support;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * One container per engine for the whole test JVM. Spring caches a context across test classes, so a container
 * stopped at the end of one class would leave the next class with a context pointing at a dead port.
 */
public final class Containers {

    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6"))
                    .withDatabaseName("orders")
                    .withUsername("inbox")
                    .withPassword("inbox");

    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));

    private Containers() {
    }

    public static void startPostgres() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    public static void startKafka() {
        if (!KAFKA.isRunning()) {
            KAFKA.start();
        }
    }
}
