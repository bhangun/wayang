package tech.kayys.wayang.knowledge.exchange;

import java.util.Map;
import java.util.Set;

public record KnowledgeEvidenceExchangeCapabilities(
        Set<KnowledgeEvidenceExchangeOperation> operations,
        Set<String> hashAlgorithms,
        Set<String> sealAlgorithms,
        boolean streaming,
        boolean partialVerification,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeCapabilities {
        operations = operations == null ? Set.of() : Set.copyOf(operations);
        hashAlgorithms = hashAlgorithms == null ? Set.of() : Set.copyOf(hashAlgorithms);
        sealAlgorithms = sealAlgorithms == null ? Set.of() : Set.copyOf(sealAlgorithms);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeEvidenceExchangeCapabilities defaults() {
        return new KnowledgeEvidenceExchangeCapabilities(
                Set.of(KnowledgeEvidenceExchangeOperation.values()),
                Set.of("SHA-256"),
                Set.of("ED25519"),
                true,
                true,
                Map.of()
        );
    }
}
