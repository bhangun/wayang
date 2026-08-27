package tech.kayys.wayang.provider.routing;

/**
 * Default multi-dimensional scoring implementation balancing quality, cost, latency, and health.
 */
public class DefaultModelScorer implements ModelScorer {

    @Override
    public InferencePlan.ScoredCandidate score(
            ModelSpec spec,
            InferenceRequirements requirements,
            InferencePolicy policy,
            ModelRoutingTelemetry telemetry
    ) {
        // 1. Quality Component (0.0 to 1.0)
        double quality = spec.qualityScore();
        if (requirements.requiresReasoning() && spec.supportsReasoning()) {
            quality = Math.min(1.0, quality + 0.15); // bonus for native reasoning capability
        }

        // 2. Cost Component (0.0 to 1.0)
        long estimatedInputTokens = Math.max(requirements.requiredContextTokens(), 2048);
        long estimatedOutputTokens = 1024;
        double estimatedCost = spec.estimateCost(estimatedInputTokens, estimatedOutputTokens);

        double costScore;
        if (spec.isLocal() || estimatedCost == 0.0) {
            costScore = 1.0;
        } else {
            // Decays as cost increases: e.g. $0.001 -> ~0.99, $0.05 -> ~0.66, $0.50 -> ~0.16
            costScore = 1.0 / (1.0 + estimatedCost * 10.0);
        }

        // 3. Latency Component (0.0 to 1.0)
        long latencyMs = spec.latencyP50Ms();
        if (telemetry != null) {
            var stats = telemetry.getStats(spec.modelId());
            if (stats != null && stats.successCalls.get() > 2) {
                latencyMs = stats.averageLatencyMs();
            }
        }
        double latencyScore = 1000.0 / (1000.0 + latencyMs);

        // 4. Objective Weights
        double wQuality;
        double wCost;
        double wLatency;
        double localBonus = 0.0;

        switch (policy.objective()) {
            case COST_MINIMIZING -> {
                wQuality = 0.15;
                wCost = 0.65;
                wLatency = 0.20;
            }
            case QUALITY_FIRST -> {
                wQuality = 0.85;
                wCost = 0.05;
                wLatency = 0.10;
            }
            case LATENCY_FIRST -> {
                wQuality = 0.20;
                wCost = 0.15;
                wLatency = 0.65;
            }
            case LOCAL_FIRST -> {
                wQuality = 0.25;
                wCost = 0.40;
                wLatency = 0.35;
                if (spec.isLocal()) localBonus = 0.30;
            }
            case BALANCED -> {
                wQuality = 0.40;
                wCost = 0.35;
                wLatency = 0.25;
            }
            default -> {
                wQuality = 0.34;
                wCost = 0.33;
                wLatency = 0.33;
            }
        }

        double baseScore = (wQuality * quality) + (wCost * costScore) + (wLatency * latencyScore) + localBonus;

        // 5. Health Multiplier from Telemetry
        double healthMultiplier = telemetry != null ? telemetry.getHealthMultiplier(spec.modelId()) : 1.0;
        double totalScore = baseScore * healthMultiplier;

        return new InferencePlan.ScoredCandidate(
                spec,
                totalScore,
                quality,
                costScore,
                latencyScore,
                healthMultiplier
        );
    }
}
