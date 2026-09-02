package tech.kayys.wayang.memory.visual;

import java.util.List;
import java.util.Map;

/**
 * Visual representation of the agent's current working memory state:
 * active task, token budget, attention distribution across facts and recent episodic memories.
 */
public record MemoryWorkingState(
        String sessionId,
        String currentTask,
        long tokenBudgetRemaining,
        Map<String, Double> attentionWeights,
        List<String> activeFacts,
        List<String> activePatterns
) {
    public MemoryWorkingState {
        attentionWeights = attentionWeights == null ? Map.of() : Map.copyOf(attentionWeights);
        activeFacts = activeFacts == null ? List.of() : List.copyOf(activeFacts);
        activePatterns = activePatterns == null ? List.of() : List.copyOf(activePatterns);
    }

    public static MemoryWorkingState empty() {
        return new MemoryWorkingState("default", "Idle", 4096L, Map.of(), List.of(), List.of());
    }
}
