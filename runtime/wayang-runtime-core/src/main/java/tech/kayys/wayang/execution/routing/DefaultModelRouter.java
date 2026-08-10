package tech.kayys.wayang.execution.routing;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.inference.ModelInfo;

import java.util.Map;
import java.util.Set;

/**
 * Rule-based default {@link ModelRouter}.
 *
 * <p>Routes based on the {@link ModelSelector#taskType()} hint and
 * {@link ModelSelector#requiredCapabilities()}. In a production system this
 * would query a registry of live providers with real cost/latency data.</p>
 */
@ApplicationScoped
public class DefaultModelRouter implements ModelRouter {

    /** Statically defined model catalogue — replace with a live registry. */
    private static final ModelInfo CODING_MODEL = new ModelInfo(
        "claude-3-7-sonnet", "Claude 3.7 Sonnet", "anthropic",
        Set.of("code", "reasoning", "tool_calling"), Map.of("costPer1M", 3_000_000L)
    );

    private static final ModelInfo FAST_MODEL = new ModelInfo(
        "gemini-2.0-flash", "Gemini 2.0 Flash", "google",
        Set.of("tool_calling", "reasoning"), Map.of("costPer1M", 350_000L, "maxLatencyMs", 400)
    );

    private static final ModelInfo MULTIMODAL_MODEL = new ModelInfo(
        "gemini-2.5-pro", "Gemini 2.5 Pro", "google",
        Set.of("vision", "code", "reasoning", "tool_calling"), Map.of("costPer1M", 7_000_000L)
    );

    private static final ModelInfo DEFAULT_MODEL = new ModelInfo(
        "gemini-2.0-flash", "Gemini 2.0 Flash", "google",
        Set.of("tool_calling"), Map.of()
    );

    @Override
    public ModelInfo select(AgentContext context, ModelSelector selector) {
        // Multi-modal check
        if (selector.requiresMultiModal()) {
            return MULTIMODAL_MODEL;
        }
        // Task-type routing
        if ("coding".equals(selector.taskType()) || "reasoning".equals(selector.taskType())) {
            return CODING_MODEL;
        }
        // Fast/cheap for classification or low-latency tasks
        if ("classification".equals(selector.taskType())
            || (selector.maxLatencyMs() > 0 && selector.maxLatencyMs() < 600)) {
            return FAST_MODEL;
        }
        // Required capabilities
        if (selector.requiredCapabilities() != null
            && selector.requiredCapabilities().contains("vision")) {
            return MULTIMODAL_MODEL;
        }
        // Cost constraint: pick fast/cheap model
        if (selector.maxCostMicros() > 0 && selector.maxCostMicros() < 1_000_000) {
            return FAST_MODEL;
        }
        return DEFAULT_MODEL;
    }

    @Override
    public ModelInfo fallback(AgentContext context, ModelInfo failed, ModelSelector selector) {
        // Always fall back to the fast model unless that's already failing
        if (FAST_MODEL.id().equals(failed.id())) {
            return null; // no further fallback
        }
        return FAST_MODEL;
    }
}
