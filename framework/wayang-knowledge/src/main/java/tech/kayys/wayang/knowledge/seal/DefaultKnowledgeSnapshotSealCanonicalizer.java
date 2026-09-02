package tech.kayys.wayang.knowledge.seal;

import java.nio.charset.StandardCharsets;

public final class DefaultKnowledgeSnapshotSealCanonicalizer implements KnowledgeSnapshotSealCanonicalizer {

    @Override
    public byte[] canonicalize(KnowledgeSnapshotSealPayload payload) {
        String canonical = "snapshotId=" + payload.snapshotId() + "\n"
                + "snapshotFingerprint=" + payload.snapshotFingerprint() + "\n"
                + "integrityStatus=" + payload.integrityStatus() + "\n"
                + "verifierId=" + payload.verifierId() + "\n"
                + "verifierVersion=" + payload.verifierVersion() + "\n"
                + "verifiedAt=" + payload.verifiedAt();

        return canonical.getBytes(StandardCharsets.UTF_8);
    }
}
