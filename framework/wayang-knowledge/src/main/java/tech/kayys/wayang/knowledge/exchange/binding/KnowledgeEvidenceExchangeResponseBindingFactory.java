package tech.kayys.wayang.knowledge.exchange.binding;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeResponse;
import tech.kayys.wayang.knowledge.exchange.session.KnowledgeEvidenceExchangeRequestBinding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class KnowledgeEvidenceExchangeResponseBindingFactory {

    private final KnowledgeEvidenceExchangeResponseFingerprinter fingerprinter;
    private final KnowledgeEvidenceExchangeResponseIdGenerator responseIdGenerator;

    public KnowledgeEvidenceExchangeResponseBindingFactory(
            KnowledgeEvidenceExchangeResponseFingerprinter fingerprinter,
            KnowledgeEvidenceExchangeResponseIdGenerator responseIdGenerator
    ) {
        this.fingerprinter = Objects.requireNonNull(fingerprinter, "fingerprinter");
        this.responseIdGenerator = Objects.requireNonNull(responseIdGenerator, "responseIdGenerator");
    }

    public KnowledgeEvidenceExchangeResponseBinding create(
            KnowledgeEvidenceExchangeRequestBinding requestBinding,
            KnowledgeEvidenceExchangeResponse response,
            String runtimeId,
            String remoteRuntimeId,
            Duration lifetime,
            Instant now
    ) {
        String responseId = responseIdGenerator.generate();
        Instant expiresAt = now.plus(lifetime != null ? lifetime : Duration.ofMinutes(15));

        KnowledgeEvidenceExchangeResponseBinding provisional = new KnowledgeEvidenceExchangeResponseBinding(
                requestBinding.requestId(),
                requestBinding.sessionId(),
                requestBinding.nonce(),
                responseId,
                runtimeId,
                remoteRuntimeId,
                requestBinding.tenantId(),
                requestBinding.workspaceId(),
                requestBinding.projectId(),
                requestBinding.operation(),
                response.artifactId(),
                response.metadata().get("resourceId"),
                response.success(),
                fingerprintContent(response),
                fingerprintManifest(response),
                fingerprintMerkleProof(response),
                now,
                expiresAt,
                "pending",
                response.metadata()
        );

        String fingerprint = fingerprinter.fingerprint(provisional);

        return new KnowledgeEvidenceExchangeResponseBinding(
                requestBinding.requestId(),
                requestBinding.sessionId(),
                requestBinding.nonce(),
                responseId,
                runtimeId,
                remoteRuntimeId,
                requestBinding.tenantId(),
                requestBinding.workspaceId(),
                requestBinding.projectId(),
                requestBinding.operation(),
                response.artifactId(),
                response.metadata().get("resourceId"),
                response.success(),
                fingerprintContent(response),
                fingerprintManifest(response),
                fingerprintMerkleProof(response),
                now,
                expiresAt,
                fingerprint,
                response.metadata()
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
}
