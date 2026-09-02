package tech.kayys.wayang.knowledge.exchange.capability;

import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.knowledge.seal.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.pack.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.exchange.session.*;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.envelope.*;
import tech.kayys.wayang.knowledge.exchange.trust.*;
import tech.kayys.wayang.knowledge.exchange.identity.*;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;
import tech.kayys.wayang.knowledge.exchange.framing.*;


import java.time.Instant;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeCapabilityNegotiationService
        implements KnowledgeEvidenceExchangeCapabilityNegotiationService {

    private final KnowledgeEvidenceExchangeCapabilityNegotiator negotiator;

    public DefaultKnowledgeEvidenceExchangeCapabilityNegotiationService(
            KnowledgeEvidenceExchangeCapabilityNegotiator negotiator
    ) {
        this.negotiator =
                Objects.requireNonNull(negotiator);
    }

    @Override
    public KnowledgeEvidenceExchangeCapabilityNegotiationResult negotiate(
            KnowledgeEvidenceExchangeCapabilityNegotiationRequest request
    ) {

        Objects.requireNonNull(request);

        return negotiator.negotiate(
                request.localManifest(),
                request.remoteManifest(),
                request.requestedAt()
        );
    }

    @Override
    public boolean isAllowed(
            KnowledgeEvidenceExchangeCapabilityNegotiationResult result,
            KnowledgeEvidenceExchangeCapabilityType capability
    ) {

        if (!result.successful()) {
            return false;
        }

        return result.capabilities()
                .stream()
                .anyMatch(
                        value -> value.type() == capability
                );
    }

    @Override
    public Instant negotiatedAt(
            KnowledgeEvidenceExchangeCapabilityNegotiationResult result
    ) {

        return Instant.now();
    }
}
