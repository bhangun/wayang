package tech.kayys.wayang.execution.governance;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-scoped registry of per-tenant/user {@link ToolBudget} instances (§34).
 *
 * <p>Budgets are keyed by {@code tenantId:userId}. Null tenant/user collapses to
 * the {@link ToolBudget#unlimited()} singleton (standalone mode).</p>
 */
@ApplicationScoped
public class ToolBudgetLedger {

    private static final String UNLIMITED_KEY = "*:*";

    private final Map<String, ToolBudget> budgets = new ConcurrentHashMap<>();

    /** Register or replace a budget for the given tenant+user pair. */
    public void register(ToolBudget budget) {
        String key = key(budget.tenantId(), budget.userId());
        budgets.put(key, budget);
    }

    /**
     * Retrieve the budget for the given tenant/user context.
     * Returns {@link ToolBudget#unlimited()} if no budget is configured.
     */
    public ToolBudget budgetFor(String tenantId, String userId) {
        ToolBudget specific = budgets.get(key(tenantId, userId));
        if (specific != null) return specific;
        // Fallback to tenant-wide budget
        ToolBudget tenantWide = budgets.get(key(tenantId, null));
        if (tenantWide != null) return tenantWide;
        return ToolBudget.unlimited();
    }

    /** Returns the budget if explicitly registered; empty otherwise. */
    public Optional<ToolBudget> find(String tenantId, String userId) {
        return Optional.ofNullable(budgets.get(key(tenantId, userId)));
    }

    public void remove(String tenantId, String userId) {
        budgets.remove(key(tenantId, userId));
    }

    public int registeredCount() { return budgets.size(); }

    private static String key(String tenantId, String userId) {
        return (tenantId != null ? tenantId : "*") + ":" + (userId != null ? userId : "*");
    }
}
