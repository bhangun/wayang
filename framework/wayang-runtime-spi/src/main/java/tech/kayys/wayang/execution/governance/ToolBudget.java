package tech.kayys.wayang.execution.governance;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-tenant/user tool execution budget (§34 — Tenant/user budgets).
 *
 * <p>Tracks remaining calls and cost against configured maximums.
 * Thread-safe via atomic counters.</p>
 *
 * @param tenantId      Owning tenant; null means global/standalone.
 * @param userId        Owning user; null means tenant-wide budget.
 * @param maxCalls      Maximum allowed tool invocations (-1 = unlimited).
 * @param maxCostUsd    Maximum allowed cost in USD (-1.0 = unlimited).
 * @param maxDurationMs Maximum total accumulated tool execution time in ms (-1 = unlimited).
 */
public final class ToolBudget {

    private final String tenantId;
    private final String userId;
    private final long   maxCalls;
    private final double maxCostUsd;
    private final long   maxDurationMs;

    private final AtomicLong usedCalls       = new AtomicLong();
    private final AtomicLong usedDurationMs  = new AtomicLong();
    private volatile double  usedCostUsd     = 0.0;

    public ToolBudget(String tenantId, String userId, long maxCalls, double maxCostUsd, long maxDurationMs) {
        this.tenantId     = tenantId;
        this.userId       = userId;
        this.maxCalls     = maxCalls;
        this.maxCostUsd   = maxCostUsd;
        this.maxDurationMs = maxDurationMs;
    }

    /** Unlimited budget — for standalone/dev mode. */
    public static ToolBudget unlimited() {
        return new ToolBudget(null, null, -1, -1.0, -1);
    }

    /** Standard enterprise tier: 1000 calls, $10 USD, 1 hour of tool time. */
    public static ToolBudget standard(String tenantId, String userId) {
        return new ToolBudget(tenantId, userId, 1000, 10.0, 3_600_000);
    }

    /**
     * Attempts to consume one tool call.
     * @return true if budget allows it; false if the budget is exhausted.
     */
    public synchronized boolean consume(long durationMs, double costUsd) {
        if (maxCalls != -1 && usedCalls.get() >= maxCalls)           return false;
        if (maxDurationMs != -1 && usedDurationMs.get() + durationMs > maxDurationMs) return false;
        if (maxCostUsd != -1.0 && usedCostUsd + costUsd > maxCostUsd) return false;

        usedCalls.incrementAndGet();
        usedDurationMs.addAndGet(durationMs);
        usedCostUsd += costUsd;
        return true;
    }

    // --- Reads ---
    public String tenantId()       { return tenantId; }
    public String userId()         { return userId; }
    public long maxCalls()         { return maxCalls; }
    public long usedCalls()        { return usedCalls.get(); }
    public double maxCostUsd()     { return maxCostUsd; }
    public double usedCostUsd()    { return usedCostUsd; }
    public long maxDurationMs()    { return maxDurationMs; }
    public long usedDurationMs()   { return usedDurationMs.get(); }

    public boolean isCallBudgetExhausted() {
        return maxCalls != -1 && usedCalls.get() >= maxCalls;
    }

    @Override
    public String toString() {
        return String.format("ToolBudget{tenant=%s, user=%s, calls=%d/%s, cost=%.4f/%s}",
            tenantId, userId,
            usedCalls.get(), maxCalls == -1 ? "∞" : String.valueOf(maxCalls),
            usedCostUsd, maxCostUsd == -1.0 ? "∞" : String.valueOf(maxCostUsd));
    }
}
