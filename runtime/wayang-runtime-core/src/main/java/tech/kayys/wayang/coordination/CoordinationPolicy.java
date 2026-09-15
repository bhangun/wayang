package tech.kayys.wayang.coordination;

import java.time.Duration;

/**
 * Runtime governance policy for multi-agent coordination.
 *
 * <p>Policies constrain coordination independently from model/inference
 * execution budgets.</p>
 */
public record CoordinationPolicy(
        int maxAgents,
        int maxDepth,
        int maxIterations,
        Duration timeout,
        boolean dynamicAgentDiscovery,
        boolean parallelExecution
) {

    public CoordinationPolicy {
        if (maxAgents < 1) {
            throw new IllegalArgumentException("maxAgents must be >= 1");
        }
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be >= 1");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be >= 1");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    public static CoordinationPolicy defaults() {
        return new CoordinationPolicy(
                8,
                4,
                10,
                Duration.ofMinutes(5),
                false,
                true
        );
    }
}
