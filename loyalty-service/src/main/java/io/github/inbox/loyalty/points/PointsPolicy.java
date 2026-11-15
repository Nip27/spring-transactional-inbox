package io.github.inbox.loyalty.points;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * How many points an order earns: one point per whole unit of its total, whatever the currency.
 */
@Component
public class PointsPolicy {

    public long pointsFor(BigDecimal totalAmount) {
        return totalAmount.setScale(0, RoundingMode.DOWN).longValueExact();
    }
}
