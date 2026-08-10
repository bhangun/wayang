package tech.kayys.wayang.execution.routing;

import java.util.Set;

/**
 * Criteria used by a {@link ModelRouter} to select the best model for a given
 * agent turn.
 *
 * <p>All fields are optional. A {@code null} value means "no constraint".</p>
 */
public record ModelSelector(
    /** Preferred task type hint (e.g. "coding", "reasoning", "classification"). */
    String taskType,
    /** Upper bound on input token window needed. */
    Long minContextWindow,
    /** Whether the selected model must support tool/function calling. */
    boolean requiresToolCalling,
    /** Whether multi-modal input (image, video) is needed. */
    boolean requiresMultiModal,
    /** Maximum cost per 1M tokens in micro-USD. 0 = no constraint. */
    long maxCostMicros,
    /** Maximum acceptable latency in ms. 0 = no constraint. */
    int maxLatencyMs,
    /** Explicit tenant data-residency region (e.g. "eu-west-1"). Null = any. */
    String dataResidencyRegion,
    /** Capabilities that the model must support (e.g. "vision", "code"). */
    Set<String> requiredCapabilities
) {
    public static ModelSelector defaults() {
        return new ModelSelector(null, null, false, false, 0, 0, null, Set.of());
    }

    public static ModelSelector forCoding() {
        return new ModelSelector("coding", 100_000L, true, false, 0, 0, null,
            Set.of("code", "reasoning"));
    }

    public static ModelSelector forClassification() {
        return new ModelSelector("classification", null, false, false, 0, 500, null, Set.of());
    }
}
