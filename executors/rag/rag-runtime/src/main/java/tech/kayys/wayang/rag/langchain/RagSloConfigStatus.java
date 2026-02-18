package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record RagSloConfigStatus(
        RagSloThresholds thresholds,
        Instant refreshedAt) {
}
