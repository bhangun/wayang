package tech.kayys.wayang.execution;

import java.util.Map;

/**
 * Always condition - always returns true.
 */
public final class AlwaysCondition implements EdgeCondition {

    public static final AlwaysCondition INSTANCE = new AlwaysCondition();

    public AlwaysCondition() {
    }

    @Override
    public ConditionType type() {
        return ConditionType.ALWAYS;
    }

    @Override
    public boolean evaluate(ExecutionContext context, NodeResult sourceResult) {
        return true;
    }

    @Override
    public Map<String, Object> parameters() {
        return Map.of();
    }
}
