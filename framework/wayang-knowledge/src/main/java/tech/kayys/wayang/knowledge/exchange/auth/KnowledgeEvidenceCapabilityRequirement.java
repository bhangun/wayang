package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;

import java.util.Map;
import java.util.Set;

public record KnowledgeEvidenceCapabilityRequirement(
        Set<KnowledgeEvidenceExchangeOperation> operations,
        Set<String> hashAlgorithms,
        Set<String> sealAlgorithms,
        boolean streaming,
        boolean partialVerification,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceCapabilityRequirement {
        operations = operations == null ? Set.of() : Set.copyOf(operations);
        hashAlgorithms = hashAlgorithms == null ? Set.of() : Set.copyOf(hashAlgorithms);
        sealAlgorithms = sealAlgorithms == null ? Set.of() : Set.copyOf(sealAlgorithms);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
