package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeCapabilities;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DefaultKnowledgeEvidenceCapabilityNegotiator
        implements KnowledgeEvidenceCapabilityNegotiator {

    @Override
    public KnowledgeEvidenceCapabilityNegotiationResult negotiate(
            KnowledgeEvidenceExchangeCapabilities remote,
            KnowledgeEvidenceCapabilityRequirement required
    ) {
        Set<String> missing = new HashSet<>();

        for (KnowledgeEvidenceExchangeOperation operation : required.operations()) {
            if (!remote.operations().contains(operation)) {
                missing.add("operation:" + operation);
            }
        }

        for (String algorithm : required.hashAlgorithms()) {
            if (!remote.hashAlgorithms().contains(algorithm)) {
                missing.add("hash:" + algorithm);
            }
        }

        for (String algorithm : required.sealAlgorithms()) {
            if (!remote.sealAlgorithms().contains(algorithm)) {
                missing.add("seal:" + algorithm);
            }
        }

        if (required.streaming() && !remote.streaming()) {
            missing.add("streaming");
        }

        if (required.partialVerification() && !remote.partialVerification()) {
            missing.add("partial-verification");
        }

        return new KnowledgeEvidenceCapabilityNegotiationResult(
                missing.isEmpty(),
                intersection(required.operations(), remote.operations()),
                intersection(required.hashAlgorithms(), remote.hashAlgorithms()),
                intersection(required.sealAlgorithms(), remote.sealAlgorithms()),
                required.streaming() && remote.streaming(),
                required.partialVerification() && remote.partialVerification(),
                missing,
                Map.of()
        );
    }

    private <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<>(a);
        result.retainAll(b);
        return Set.copyOf(result);
    }
}
