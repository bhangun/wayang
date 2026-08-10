package tech.kayys.wayang.execution;

import java.time.Duration;

/**
 * Defines the resource limits for an agent execution loop.
 */
public record ExecutionBudget(
    Duration maxDuration,
    int maxSteps,
    long maxToolCalls,
    long maxInputTokens,
    long maxOutputTokens
) {
    public static ExecutionBudget defaults() {
        return new ExecutionBudget(
            Duration.ofMinutes(5),
            25,
            50,
            200_000,
            50_000
        );
    }
}
