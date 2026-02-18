package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;
import java.util.List;

public record RagSloStatus(
        boolean healthy,
        RagSloThresholds thresholds,
        RagSloSnapshot snapshot,
        List<RagSloBreach> breaches,
        Instant evaluatedAt) {
}
