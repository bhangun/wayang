package tech.kayys.wayang.knowledge.exchange.binding;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeResponse;
import tech.kayys.wayang.knowledge.exchange.session.KnowledgeEvidenceExchangeRequestBinding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeResponseVerifier
        implements KnowledgeEvidenceExchangeResponseVerifier {

    private final KnowledgeEvidenceExchangeResponseFingerprinter fingerprinter;

    public DefaultKnowledgeEvidenceExchangeResponseVerifier(
            KnowledgeEvidenceExchangeResponseFingerprinter fingerprinter
    ) {
        this.fingerprinter = Objects.requireNonNull(fingerprinter, "fingerprinter");
    }

    @Override
    public KnowledgeEvidenceExchangeResponseVerificationResult verify(
            KnowledgeEvidenceExchangeRequestBinding requestBinding,
            KnowledgeEvidenceExchangeResponseBinding responseBinding,
            KnowledgeEvidenceExchangeResponse response,
            String expectedRemoteRuntimeId,
            Instant now
    ) {
        if (!requestBinding.requestId().equals(responseBinding.requestId())) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.REQUEST_MISMATCH, "Response is bound to a different request");
        }

        if (!requestBinding.sessionId().equals(responseBinding.sessionId())) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.SESSION_MISMATCH, "Response is bound to a different session");
        }

        if (!requestBinding.nonce().equals(responseBinding.requestNonce())) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.NONCE_MISMATCH, "Response nonce does not match request");
        }

        if (expectedRemoteRuntimeId != null && !expectedRemoteRuntimeId.equals(responseBinding.runtimeId())) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.RUNTIME_MISMATCH, "Response originated from an unexpected runtime");
        }

        if (requestBinding.operation() != responseBinding.operation()) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.OPERATION_MISMATCH, "Response operation does not match request");
        }

        if (requestBinding.artifactId() != null) {
            String respArtId = responseBinding.artifactId() != null ? responseBinding.artifactId().value() : null;
            if (!requestBinding.artifactId().equals(respArtId)) {
                return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.ARTIFACT_MISMATCH, "Response artifact does not match request");
            }
        }

        if (responseBinding.isExpired(now)) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.EXPIRED, "Response binding has expired");
        }

        String actualFingerprint = fingerprinter.fingerprint(
                new KnowledgeEvidenceExchangeResponseBinding(
                        responseBinding.requestId(),
                        responseBinding.sessionId(),
                        responseBinding.requestNonce(),
                        responseBinding.responseId(),
                        responseBinding.runtimeId(),
                        responseBinding.remoteRuntimeId(),
                        responseBinding.tenantId(),
                        responseBinding.workspaceId(),
                        responseBinding.projectId(),
                        responseBinding.operation(),
                        response.artifactId(),
                        responseBinding.resourceId(),
                        response.success(),
                        fingerprintContent(response),
                        fingerprintManifest(response),
                        fingerprintMerkleProof(response),
                        responseBinding.issuedAt(),
                        responseBinding.expiresAt(),
                        "pending",
                        responseBinding.metadata()
                )
        );

        if (!actualFingerprint.equals(responseBinding.responseFingerprint())) {
            return invalid(KnowledgeEvidenceExchangeResponseVerificationStatus.FINGERPRINT_MISMATCH, "Response fingerprint does not match");
        }

        return new KnowledgeEvidenceExchangeResponseVerificationResult(
                KnowledgeEvidenceExchangeResponseVerificationStatus.VALID,
                requestBinding.requestId(),
                responseBinding.responseId(),
                responseBinding.responseFingerprint(),
                actualFingerprint,
                "OK",
                Map.of()
        );
    }

    private String fingerprintContent(KnowledgeEvidenceExchangeResponse response) {
        if (response.content() == null || response.content().length == 0) {
            return null;
        }
        return sha256(response.content());
    }

    private String fingerprintManifest(KnowledgeEvidenceExchangeResponse response) {
        if (response.manifest() == null) {
            return null;
        }
        return sha256(response.manifest().toString().getBytes(StandardCharsets.UTF_8));
    }

    private String fingerprintMerkleProof(KnowledgeEvidenceExchangeResponse response) {
        if (response.merkleProof() == null) {
            return null;
        }
        return sha256(response.merkleProof().toString().getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder result = new StringBuilder("sha256:");
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private KnowledgeEvidenceExchangeResponseVerificationResult invalid(
            KnowledgeEvidenceExchangeResponseVerificationStatus status,
            String reason
    ) {
        return new KnowledgeEvidenceExchangeResponseVerificationResult(
                status, null, null, null, null, reason, Map.of()
        );
    }
}
