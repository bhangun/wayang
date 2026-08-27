package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.provider.Provider;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Complete, explainable inference decision outcome produced by the ModelRouter.
 */
public record InferencePlan(
        String requestId,
        String selectedModel,
        Provider selectedProvider,
        InferenceRequirements requirements,
        InferencePolicy policy,
        List<ScoredCandidate> scoredCandidates,
        Map<String, String> rejectedCandidates,
        List<FallbackTarget> fallbackTargets,
        double estimatedCost,
        String decisionReason,
        Instant plannedAt
) {
    public record ScoredCandidate(
            ModelSpec modelSpec,
            double totalScore,
            double qualityComponent,
            double costComponent,
            double latencyComponent,
            double healthMultiplier
    ) {}

    public record FallbackTarget(
            String modelId,
            Provider provider,
            String reason
    ) {}

    public static InferencePlan direct(String requestId, String selectedModel, Provider provider, String reason) {
        return new InferencePlan(
                requestId,
                selectedModel,
                provider,
                InferenceRequirements.defaults(),
                InferencePolicy.defaults(),
                List.of(),
                Map.of(),
                List.of(),
                0.0,
                reason,
                Instant.now()
        );
    }
}
