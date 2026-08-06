package tech.kayys.wayang.execution;

import java.util.Map;

/**
 * Failure condition - returns true if the source node failed.
 */
public final class FailureCondition implements EdgeCondition {

    public static final FailureCondition INSTANCE = new FailureCondition();

    public FailureCondition() {
    }

    @Override
    public ConditionType type() {
        return ConditionType.FAILURE;
    }

    @Override
    public boolean evaluate(ExecutionContext context, NodeResult sourceResult) {
        return sourceResult != null && !sourceResult.isSuccess();
    }

    @Override
    public Map<String, Object> parameters() {
        return Map.of();
    }
}