package tech.kayys.wayang.execution;

import java.util.Map;

/**
 * Success condition - returns true if the source node succeeded.
 */
public final class SuccessCondition implements EdgeCondition {

    public static final SuccessCondition INSTANCE = new SuccessCondition();

    public SuccessCondition() {
    }

    @Override
    public ConditionType type() {
        return ConditionType.SUCCESS;
    }

    @Override
    public boolean evaluate(ExecutionContext context, NodeResult sourceResult) {
        return sourceResult != null && sourceResult.isSuccess();
    }

    @Override
    public Map<String, Object> parameters() {
        return Map.of();
    }
}
