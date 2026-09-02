package tech.kayys.wayang.knowledge.replay;

import java.time.Instant;
import java.util.Map;

public record KnowledgeReplayRequest(
        String traceId,
        KnowledgeReplayMode mode,
        Instant effectiveAt,
        boolean verifyEvidence,
        boolean verifyLineage,
        boolean verifyPolicies,
        boolean verifyRules,
        Map<String, Object> metadata
) {

    public KnowledgeReplayRequest {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId is required");
        }
        mode = mode == null ? KnowledgeReplayMode.EXACT : mode;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeReplayRequest exact(String traceId) {
        return new KnowledgeReplayRequest(
                traceId,
                KnowledgeReplayMode.EXACT,
                null,
                true,
                true,
                true,
                true,
                Map.of()
        );
    }
}
