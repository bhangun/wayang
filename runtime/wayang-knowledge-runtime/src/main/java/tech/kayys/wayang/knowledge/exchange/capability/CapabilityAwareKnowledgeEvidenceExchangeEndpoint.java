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

public final class CapabilityAwareKnowledgeEvidenceExchangeEndpoint
        implements KnowledgeEvidenceExchangeEndpoint {

    private final KnowledgeEvidenceExchangeEndpoint delegate;

    private final KnowledgeEvidenceExchangeNegotiatedSessionValidator
            sessionValidator;

    private final KnowledgeEvidenceExchangeNegotiatedSession session;

    public CapabilityAwareKnowledgeEvidenceExchangeEndpoint(
            KnowledgeEvidenceExchangeEndpoint delegate,
            KnowledgeEvidenceExchangeNegotiatedSessionValidator
                    sessionValidator,
            KnowledgeEvidenceExchangeNegotiatedSession session
    ) {

        this.delegate =
                Objects.requireNonNull(delegate);

        this.sessionValidator =
                Objects.requireNonNull(sessionValidator);

        this.session =
                Objects.requireNonNull(session);
    }

    @Override
    public KnowledgeEvidenceExchangeResponse exchange(
            KnowledgeEvidenceExchangeRequest request
    ) {

        KnowledgeEvidenceExchangeCapabilityType capability =
                capabilityFor(request.operation());

        sessionValidator.require(
                session,
                capability,
                Instant.now()
        );

        return delegate.exchange(request);
    }

    private KnowledgeEvidenceExchangeCapabilityType capabilityFor(
            KnowledgeEvidenceExchangeOperation operation
    ) {

        return switch (operation) {

            case RESOLVE_ARTIFACT ->
                    KnowledgeEvidenceExchangeCapabilityType
                            .ARTIFACT_RESOLUTION;

            case GET_MANIFEST ->
                    KnowledgeEvidenceExchangeCapabilityType
                            .MANIFEST_RETRIEVAL;

            case GET_RESOURCE ->
                    KnowledgeEvidenceExchangeCapabilityType
                            .RESOURCE_RETRIEVAL;

            case GET_MERKLE_PROOF ->
                    KnowledgeEvidenceExchangeCapabilityType
                            .MERKLE_PROOF;

            case VERIFY_ARTIFACT,
                 VERIFY_RESOURCE ->
                    KnowledgeEvidenceExchangeCapabilityType
                            .INTEGRITY_VERIFICATION;
        };
    }
}
