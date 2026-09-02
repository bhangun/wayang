package tech.kayys.wayang.knowledge.exchange.envelope;

import java.nio.charset.StandardCharsets;

public final class DefaultKnowledgeEvidenceExchangeMessageCanonicalizer
        implements KnowledgeEvidenceExchangeMessageCanonicalizer {

    @Override
    public byte[] canonicalize(KnowledgeEvidenceExchangeSignedEnvelope envelope) {
        String canonical = String.join("|",
                safe(envelope.envelopeId()),
                safe(envelope.requestId()),
                safe(envelope.sessionId()),
                safe(envelope.nonce()),
                safe(envelope.runtimeId()),
                safe(envelope.remoteRuntimeId()),
                safe(envelope.tenantId()),
                safe(envelope.workspaceId()),
                safe(envelope.projectId()),
                safe(envelope.messageType()),
                safe(envelope.messageFingerprint()),
                envelope.authenticationAlgorithm() != null ? envelope.authenticationAlgorithm().name() : "",
                safe(envelope.keyId()),
                safe(envelope.keyVersion()),
                envelope.issuedAt() != null ? envelope.issuedAt().toString() : "",
                envelope.expiresAt() != null ? envelope.expiresAt().toString() : ""
        );

        return canonical.getBytes(StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
