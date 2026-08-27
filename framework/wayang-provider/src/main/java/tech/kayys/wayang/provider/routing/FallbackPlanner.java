package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.provider.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructs structured, resilient fallback execution targets according to {@link InferencePolicy.FallbackStrategy}.
 */
public class FallbackPlanner {

    public List<InferencePlan.FallbackTarget> planFallbacks(
            InferencePlan.ScoredCandidate selected,
            List<InferencePlan.ScoredCandidate> scoredCandidates,
            InferencePolicy policy,
            List<Provider> availableProviders,
            java.util.function.BiFunction<ModelSpec, List<Provider>, Provider> providerResolver
    ) {
        if (policy.fallbackStrategy() == InferencePolicy.FallbackStrategy.STRICT_NO_FALLBACK) {
            return List.of();
        }

        List<InferencePlan.FallbackTarget> fallbacks = new ArrayList<>();

        switch (policy.fallbackStrategy()) {
            case LOCAL_TO_CLOUD -> {
                // If selected was local, look for the highest scoring cloud model
                for (InferencePlan.ScoredCandidate candidate : scoredCandidates) {
                    if (candidate == selected) continue;
                    if (!candidate.modelSpec().isLocal()) {
                        Provider p = providerResolver.apply(candidate.modelSpec(), availableProviders);
                        fallbacks.add(new InferencePlan.FallbackTarget(
                                candidate.modelSpec().modelId(),
                                p,
                                "Local-to-Cloud fallback to " + candidate.modelSpec().modelId()
                        ));
                        break;
                    }
                }
            }
            case CHEAP_TO_SMART -> {
                // Fallback to highest quality candidate
                InferencePlan.ScoredCandidate highestQuality = null;
                for (InferencePlan.ScoredCandidate candidate : scoredCandidates) {
                    if (candidate == selected) continue;
                    if (highestQuality == null || candidate.qualityComponent() > highestQuality.qualityComponent()) {
                        highestQuality = candidate;
                    }
                }
                if (highestQuality != null) {
                    Provider p = providerResolver.apply(highestQuality.modelSpec(), availableProviders);
                    fallbacks.add(new InferencePlan.FallbackTarget(
                            highestQuality.modelSpec().modelId(),
                            p,
                            "Cheap-to-Smart fallback to highest quality " + highestQuality.modelSpec().modelId()
                    ));
                }
            }
            default -> {
                // ORDERED_FALLBACK: pick next top 3 candidates
                for (int i = 0; i < scoredCandidates.size() && fallbacks.size() < 3; i++) {
                    InferencePlan.ScoredCandidate candidate = scoredCandidates.get(i);
                    if (candidate.modelSpec().modelId().equalsIgnoreCase(selected.modelSpec().modelId())) {
                        continue;
                    }
                    Provider p = providerResolver.apply(candidate.modelSpec(), availableProviders);
                    fallbacks.add(new InferencePlan.FallbackTarget(
                            candidate.modelSpec().modelId(),
                            p,
                            String.format("Rank #%d fallback (score: %.3f)", fallbacks.size() + 2, candidate.totalScore())
                    ));
                }
            }
        }

        return fallbacks;
    }
}
