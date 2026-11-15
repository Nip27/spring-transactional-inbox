package io.github.inbox.loyalty.points;

/**
 * Kind of a ledger entry. An order has at most one entry of each kind.
 */
public enum LedgerEntryKind {
    /**
     * Points earned by an order.
     */
    ACCRUAL,
    /**
     * Points taken back when the order is cancelled; zero when the cancellation came before any accrual.
     */
    REVERSAL
}
