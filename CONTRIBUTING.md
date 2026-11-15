# Contributing

Thank you for your interest in contributing to **spring-transactional-inbox**.

## Before you start

* Read the [README](README.md), especially the delivery guarantees each service relies on.
* Check [open issues](https://github.com/dmitriy-iliyov/spring-transactional-inbox/issues) to avoid duplicate work.
  Issues labelled `good first issue` are self-contained and come with a clear definition of done.
* Comment on an issue before starting it - the maintainer assigns it to you, so two people do not do the same work.
* For a larger change without an issue, open one first to agree on the approach.

## Development setup

### Prerequisites

* Java 21
* Maven 3.9+ (or the bundled `./mvnw`)
* Docker - for local infrastructure and for the integration tests (Testcontainers)

### Build and test

```bash
mvn clean verify
```

This runs every test against real PostgreSQL and Kafka containers and fails if a module drops below 80% line
coverage. Run one module with `mvn -pl loyalty-service -am verify`, one test level with
`-Dtest='*IntegrationTest'`.

### Run locally

```bash
docker compose up -d
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl loyalty-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl notification-service spring-boot:run -Dspring-boot.run.profiles=dev
```

| Component | Address |
|-----------|---------|
| order-service | http://localhost:8080 |
| loyalty-service | http://localhost:8081 |
| notification-service | http://localhost:8082 |
| PostgreSQL | `localhost:5432`, user `inbox` / `inbox`, databases `orders`, `loyalty`, `notifications` |
| Kafka | `localhost:9092`, topics `orders.events`, `orders.events.DLT` |
| Mailpit | SMTP `localhost:1025`, UI http://localhost:8025 |

## Project structure

| Module | Purpose |
|--------|---------|
| `inbox-events-contract` | event types, topics, payload records |
| `order-service` | orders API, transactional outbox, `published_events` log |
| `loyalty-service` | points, effectively-once inbox (`@TransactionalConsumer`) |
| `notification-service` | confirmation e-mails, at-most-once inbox (`@NonTransactionalConsumer`) |

## Conventions

### Code

* Java 21, four-space indent, lines up to about 120 characters, no `var`, no Lombok.
* Constructor injection; controllers are thin - one service call per endpoint.
* Every error response is an RFC 9457 `ProblemDetail` built by the module's problem factory; a new failure gets a
  type constant in `*ProblemTypes`.
* An endpoint carries its OpenAPI annotations (`@Operation`, `@ApiResponses`, `@Schema` on every record component)
  once its controller is documented.
* Schema changes are new Liquibase changesets; an applied changeset is never edited.
* Versions live in the root `pom.xml` (`properties` / `dependencyManagement`), never in a child POM.

### Tests

Test classes are named by level, and a test method by what it checks:

| Suffix | Level |
|--------|-------|
| `*UnitTest` | one class, collaborators mocked |
| `*IntegrationTest` | a Spring slice or the whole service, real PostgreSQL / Kafka in Testcontainers |
| `*MultiThreadTest` | a guarantee that only exists under concurrency |

```java
@Test
@DisplayName("UT accrue() when the order was already cancelled should accrue nothing")
void accrue_whenOrderWasCancelledFirst_shouldAccrueNothing() {
    // given
    // when
    // then
}
```

AssertJ for assertions, Awaitility (through the module's `Await` helper) for anything asynchronous, never
`Thread.sleep`.

### Commits and pull requests

* Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/):
  `feat(loyalty): ...`, `fix(order): ...`, `test(notification): ...`, `docs: ...`, `build: ...`, `ci: ...` -
  short and imperative.
* One issue, one focused pull request, with `Closes #N` in its description.
* Add a line to the `[Unreleased]` section of [CHANGELOG.md](CHANGELOG.md) with the issue number and your GitHub
  handle.
* `mvn clean verify` is green locally and in CI.

## Reporting bugs and security issues

* Bugs: open an issue with the bug template.
* Security vulnerabilities: follow [SECURITY.md](SECURITY.md) - never a public issue.
