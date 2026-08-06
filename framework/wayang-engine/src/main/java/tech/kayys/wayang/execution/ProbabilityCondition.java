package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Probability condition - returns true with a given probability.
 */
public final class ProbabilityCondition implements EdgeCondition {

    private final double probability;
    private final Map<String, Object> parameters;

    public ProbabilityCondition(double probability) {
        this.probability = Math.max(0, Math.min(1, probability));
        this.parameters = Map.of("probability", this.probability);
    }

    @Override
    public ConditionType type() {
        return ConditionType.PROBABILITY;
    }

    @Override
    public boolean evaluate(ExecutionContext context, NodeResult sourceResult) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    @Override
    public Map<String, Object> parameters() {
        return parameters;
    }

    public double getProbability() {
        return probability;
    }
}