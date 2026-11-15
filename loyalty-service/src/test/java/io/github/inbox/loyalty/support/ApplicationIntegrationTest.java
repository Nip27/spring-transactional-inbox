package io.github.inbox.loyalty.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;

/**
 * Base of the tests that start the whole service: the shared Postgres and Kafka containers, one context for every
 * class extending it.
 */
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class ApplicationIntegrationTest extends PostgresIntegrationTest {

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        Containers.startKafka();
        registry.add("spring.kafka.bootstrap-servers", Containers.KAFKA::getBootstrapServers);
    }
}
