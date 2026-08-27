package tech.kayys.wayang.provider.routing;

import java.util.Set;

/**
 * Policy governing model selection objectives, constraints, and fallback preferences.
 */
public record InferencePolicy(
        RoutingObjective objective,
        Double maxCostPerTurn,
        Set<String> allowedProviders,
        Set<String> deniedProviders,
        Set<String> allowedModels,
        Set<String> deniedModels,
        FallbackStrategy fallbackStrategy
) {
    public enum RoutingObjective {
        COST_MINIMIZING,
        BALANCED,
        QUALITY_FIRST,
        LATENCY_FIRST,
        LOCAL_FIRST
    }

    public enum FallbackStrategy {
        ORDERED_FALLBACK,
        LOCAL_TO_CLOUD,
        CHEAP_TO_SMART,
        STRICT_NO_FALLBACK
    }

    public InferencePolicy {
        objective = objective != null ? objective : RoutingObjective.BALANCED;
        allowedProviders = allowedProviders != null ? Set.copyOf(allowedProviders) : Set.of();
        deniedProviders = deniedProviders != null ? Set.copyOf(deniedProviders) : Set.of();
        allowedModels = allowedModels != null ? Set.copyOf(allowedModels) : Set.of();
        deniedModels = deniedModels != null ? Set.copyOf(deniedModels) : Set.of();
        fallbackStrategy = fallbackStrategy != null ? fallbackStrategy : FallbackStrategy.ORDERED_FALLBACK;
    }

    public static InferencePolicy defaults() {
        return new InferencePolicy(RoutingObjective.BALANCED, null, Set.of(), Set.of(), Set.of(), Set.of(), FallbackStrategy.ORDERED_FALLBACK);
    }

    public static InferencePolicy fast() {
        return new InferencePolicy(RoutingObjective.LATENCY_FIRST, 0.05, Set.of(), Set.of(), Set.of(), Set.of(), FallbackStrategy.LOCAL_TO_CLOUD);
    }

    public static InferencePolicy balanced() {
        return defaults();
    }

    public static InferencePolicy thorough() {
        return new InferencePolicy(RoutingObjective.QUALITY_FIRST, null, Set.of(), Set.of(), Set.of(), Set.of(), FallbackStrategy.ORDERED_FALLBACK);
    }

    public static InferencePolicy debug() {
        return new InferencePolicy(RoutingObjective.LOCAL_FIRST, 0.0, Set.of(), Set.of(), Set.of(), Set.of(), FallbackStrategy.ORDERED_FALLBACK);
    }
}
