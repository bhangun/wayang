package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeCapabilities;

public interface KnowledgeEvidenceCapabilityNegotiator {
    KnowledgeEvidenceCapabilityNegotiationResult negotiate(
            KnowledgeEvidenceExchangeCapabilities remote,
            KnowledgeEvidenceCapabilityRequirement required
    );
}
