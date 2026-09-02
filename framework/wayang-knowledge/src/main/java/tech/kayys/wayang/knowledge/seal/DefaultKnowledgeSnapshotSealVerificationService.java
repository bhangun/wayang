package tech.kayys.wayang.knowledge.seal;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeSnapshotSealVerificationService implements KnowledgeSnapshotSealVerificationService {

    private final KnowledgeSnapshotSealCanonicalizer canonicalizer;
    private final KnowledgeSnapshotSignatureVerifier verifier;

    public DefaultKnowledgeSnapshotSealVerificationService(
            KnowledgeSnapshotSealCanonicalizer canonicalizer,
            KnowledgeSnapshotSignatureVerifier verifier) {

        this.canonicalizer = Objects.requireNonNull(canonicalizer);
        this.verifier = Objects.requireNonNull(verifier);
    }

    @Override
    public KnowledgeSnapshotSealVerificationResult verify(
            KnowledgeSnapshotSecureSeal seal,
            KnowledgeSnapshotSealPayload payload) {

        if (seal.isExpired(Instant.now())) {
            return result(seal, KnowledgeSnapshotSealStatus.EXPIRED, "Seal has expired");
        }

        if (!seal.snapshotId().value().equals(payload.snapshotId())) {
            return result(seal, KnowledgeSnapshotSealStatus.INVALID, "Snapshot ID mismatch");
        }

        if (!seal.snapshotFingerprint().equals(payload.snapshotFingerprint())) {
            return result(seal, KnowledgeSnapshotSealStatus.INVALID, "Snapshot fingerprint mismatch");
        }

        byte[] canonical = canonicalizer.canonicalize(payload);
        byte[] signature;

        try {
            signature = Base64.getDecoder().decode(seal.signature());
        } catch (IllegalArgumentException e) {
            return result(seal, KnowledgeSnapshotSealStatus.INVALID, "Malformed signature");
        }

        boolean valid = verifier.verify(canonical, signature, seal.keyId(), seal.keyVersion());
        return result(
                seal,
                valid ? KnowledgeSnapshotSealStatus.VERIFIED : KnowledgeSnapshotSealStatus.INVALID,
                valid ? "Signature verified" : "Signature verification failed"
        );
    }

    private KnowledgeSnapshotSealVerificationResult result(
            KnowledgeSnapshotSecureSeal seal,
            KnowledgeSnapshotSealStatus status,
            String message) {

        return new KnowledgeSnapshotSealVerificationResult(
                UUID.randomUUID().toString(),
                seal.snapshotId(),
                status,
                seal.keyId(),
                seal.keyVersion(),
                Instant.now(),
                List.of(message)
        );
    }
}
