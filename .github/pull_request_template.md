## Summary

<!-- What does this PR change and why? -->

## Related issues

<!-- Closes #123 -->

## Type of change

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that changes existing behaviour)
- [ ] Documentation update
- [ ] Refactoring / chore / dependency update

## Affected modules

- [ ] `inbox-events-contract`
- [ ] `order-service`
- [ ] `loyalty-service`
- [ ] `notification-service`
- [ ] `docker-compose` / infrastructure
- [ ] docs / CI / other

## Test plan

- [ ] `mvn clean verify`
- [ ] Manual testing (describe below)

**Manual steps (if any):**

```bash
# e.g. docker compose up -d && mvn -pl loyalty-service spring-boot:run -Dspring-boot.run.profiles=dev
```

## Checklist

- [ ] Code follows the conventions in CONTRIBUTING.md
- [ ] Tests added or updated where appropriate
- [ ] A line added to `[Unreleased]` in CHANGELOG.md
- [ ] Documentation updated (if user-facing behaviour changed)
- [ ] Liquibase changes are new changesets (no edits to applied ones)
- [ ] No secrets or environment-specific credentials committed
