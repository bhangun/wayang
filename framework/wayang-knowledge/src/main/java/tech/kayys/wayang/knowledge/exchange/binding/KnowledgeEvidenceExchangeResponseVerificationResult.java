package tech.kayys.wayang.knowledge.exchange.binding;

import java.util.Map;

public record KnowledgeEvidenceExchangeResponseVerificationResult(
        KnowledgeEvidenceExchangeResponseVerificationStatus status,
        String requestId,
        String responseId,
        String expectedFingerprint,
        String actualFingerprint,
        String reason,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeResponseVerificationResult {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean valid() {
        return status == KnowledgeEvidenceExchangeResponseVerificationStatus.VALID;
    }
}
