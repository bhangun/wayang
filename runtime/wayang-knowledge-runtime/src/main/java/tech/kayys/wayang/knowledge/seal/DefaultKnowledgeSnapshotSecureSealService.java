package tech.kayys.wayang.knowledge.seal;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityResult;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeSnapshotSecureSealService implements KnowledgeSnapshotSecureSealService {

    private final KnowledgeSnapshotSigner signer;
    private final KnowledgeSnapshotSealCanonicalizer canonicalizer;

    public DefaultKnowledgeSnapshotSecureSealService(
            KnowledgeSnapshotSigner signer,
            KnowledgeSnapshotSealCanonicalizer canonicalizer) {

        this.signer = Objects.requireNonNull(signer);
        this.canonicalizer = Objects.requireNonNull(canonicalizer);
    }

    @Override
    public KnowledgeSnapshotSecureSeal seal(
            KnowledgeSnapshotIntegrityResult result,
            String verifierId,
            String verifierVersion) {

        if (!result.isValid()) {
            throw new IllegalStateException("Only an ATTESTED snapshot can be sealed");
        }

        KnowledgeSnapshotSealPayload payload = new KnowledgeSnapshotSealPayload(
                result.snapshotId().value(),
                result.expectedFingerprint(),
                result.status().name(),
                verifierId,
                verifierVersion,
                result.verifiedAt()
        );

        byte[] canonical = canonicalizer.canonicalize(payload);
        byte[] signature = signer.sign(canonical);

        return new KnowledgeSnapshotSecureSeal(
                UUID.randomUUID().toString(),
                result.snapshotId(),
                result.expectedFingerprint(),
                signer.algorithm(),
                KnowledgeSnapshotTrustAnchorType.LOCAL,
                signer.keyId(),
                signer.keyVersion(),
                Base64.getEncoder().encodeToString(signature),
                Instant.now(),
                null,
                KnowledgeSnapshotSealStatus.SEALED,
                Map.of("verifierId", verifierId, "verifierVersion", verifierVersion)
        );
    }
}
