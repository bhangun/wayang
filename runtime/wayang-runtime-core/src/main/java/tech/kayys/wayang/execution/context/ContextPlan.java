package tech.kayys.wayang.execution.context;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.inference.ModelInfo;

/**
 * The result of a {@link ContextPlanner} run — the compiled context fragments
 * that will be injected into the model prompt.
 *
 * @param systemPrompt    the assembled system prompt (instructions + skills)
 * @param conversationStr serialized conversation history fragment (within budget)
 * @param memoryStr       relevant memory items (within budget)
 * @param ragStr          relevant RAG / repository context (within budget)
 * @param artifactStr     relevant artifacts from the current run (within budget)
 * @param budget          the token budget used for this plan
 * @param model           the model this plan was compiled for
 */
public record ContextPlan(
    String systemPrompt,
    String conversationStr,
    String memoryStr,
    String ragStr,
    String artifactStr,
    ContextBudget budget,
    ModelInfo model
) {
    /**
     * Produces a merged prompt string for providers that need a single input.
     */
    public String mergedContext() {
        StringBuilder sb = new StringBuilder();
        if (memoryStr != null && !memoryStr.isBlank()) {
            sb.append("## Memory\n").append(memoryStr).append("\n\n");
        }
        if (ragStr != null && !ragStr.isBlank()) {
            sb.append("## Context\n").append(ragStr).append("\n\n");
        }
        if (artifactStr != null && !artifactStr.isBlank()) {
            sb.append("## Artifacts\n").append(artifactStr).append("\n\n");
        }
        return sb.toString();
    }
}
