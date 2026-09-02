package tech.kayys.wayang.knowledge.replay;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.List;
import java.util.Map;

public record KnowledgeReplayResult(
        String replayId,
        String traceId,
        KnowledgeReplayStatus status,
        KnowledgeDecisionTrace originalTrace,
        KnowledgeDecisionTrace replayedTrace,
        List<String> divergences,
        Map<String, Object> diagnostics
) {

    public KnowledgeReplayResult {
        divergences = divergences == null ? List.of() : List.copyOf(divergences);
        diagnostics = diagnostics == null ? Map.of() : Map.copyOf(diagnostics);
    }

    public boolean reproduced() {
        return status == KnowledgeReplayStatus.REPRODUCED;
    }

    public boolean diverged() {
        return status == KnowledgeReplayStatus.DIVERGED;
    }
}
