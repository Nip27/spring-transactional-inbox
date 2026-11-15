package io.github.inbox.loyalty.points;

import io.github.inbox.loyalty.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestConstructor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LedgerRepositoryIntegrationTest extends PostgresIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

    private final LedgerEntryRepository ledger;
    private final CustomerPointsRepository balances;
    private final TestEntityManager entityManager;

    LedgerRepositoryIntegrationTest(LedgerEntryRepository ledger,
                                    CustomerPointsRepository balances,
                                    TestEntityManager entityManager) {
        this.ledger = ledger;
        this.balances = balances;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void cleanUp() {
        ledger.deleteAll();
        balances.deleteAll();
    }

    @Test
    @DisplayName("IT save() when the order already has an entry of that kind should reject the second one")
    void save_whenOrderAlreadyHasEntryOfKind_shouldRejectSecond() {
        // given
        ledger.saveAndFlush(new LedgerEntryEntity(7L, 42L, LedgerEntryKind.ACCRUAL, 21, NOW));

        // when / then
        assertThatThrownBy(() -> ledger.saveAndFlush(new LedgerEntryEntity(7L, 42L, LedgerEntryKind.ACCRUAL, 21, NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("IT findByOrderIdAndKind() when only the other kind exists should find nothing")
    void findByOrderIdAndKind_whenOnlyOtherKindExists_shouldReturnEmpty() {
        // given
        ledger.saveAndFlush(new LedgerEntryEntity(7L, 42L, LedgerEntryKind.ACCRUAL, 21, NOW));

        // when / then
        assertThat(ledger.findByOrderIdAndKind(7L, LedgerEntryKind.REVERSAL)).isEmpty();
        assertThat(ledger.findByOrderIdAndKind(7L, LedgerEntryKind.ACCRUAL)).hasValueSatisfying(
                entry -> assertThat(entry.getPoints()).isEqualTo(21));
    }

    @Test
    @DisplayName("IT save() when a balance changes should store it and raise its version")
    void save_whenBalanceChanges_shouldRaiseVersion() {
        // given
        CustomerPointsEntity balance = balances.saveAndFlush(new CustomerPointsEntity(42L, NOW));
        Long createdVersion = balance.getVersion();

        // when
        balance.add(21, NOW.plusSeconds(1));
        balances.saveAndFlush(balance);
        entityManager.clear();

        // then
        CustomerPointsEntity stored = balances.findById(42L).orElseThrow();
        assertThat(stored.getBalance()).isEqualTo(21);
        assertThat(stored.getVersion()).isGreaterThan(createdVersion);
    }
}
