package io.github.inbox.notification.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;

/**
 * Base of the tests that start the whole service: the shared Postgres and Kafka containers and the SMTP server, one
 * context for every class extending it.
 */
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class ApplicationIntegrationTest extends PostgresIntegrationTest {

    @DynamicPropertySource
    static void kafkaAndMailProperties(DynamicPropertyRegistry registry) {
        Containers.startKafka();
        MailServer.start();
        registry.add("spring.kafka.bootstrap-servers", Containers.KAFKA::getBootstrapServers);
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", MailServer::port);
    }
}
