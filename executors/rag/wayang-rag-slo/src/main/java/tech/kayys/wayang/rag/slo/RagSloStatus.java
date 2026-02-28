package tech.kayys.wayang.rag.slo;

import java.time.Instant;
import java.util.List;

public record RagSloStatus(
        boolean healthy,
        RagSloThresholds thresholds,
        RagSloSnapshot snapshot,
        List<RagSloBreach> breaches,
        Instant evaluatedAt) {
}
