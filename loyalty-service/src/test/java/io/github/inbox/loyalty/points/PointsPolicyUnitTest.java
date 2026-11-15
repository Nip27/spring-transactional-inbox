package io.github.inbox.loyalty.points;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PointsPolicyUnitTest {

    private final PointsPolicy tested = new PointsPolicy();

    @Test
    @DisplayName("UT pointsFor() when total has a fraction should earn one point per whole unit")
    void pointsFor_whenTotalHasFraction_shouldRoundDown() {
        // when / then
        assertThat(tested.pointsFor(new BigDecimal("21.99"))).isEqualTo(21);
    }

    @Test
    @DisplayName("UT pointsFor() when total is below one unit should earn nothing")
    void pointsFor_whenTotalIsBelowOneUnit_shouldEarnNothing() {
        // when / then
        assertThat(tested.pointsFor(new BigDecimal("0.99"))).isZero();
    }
}
