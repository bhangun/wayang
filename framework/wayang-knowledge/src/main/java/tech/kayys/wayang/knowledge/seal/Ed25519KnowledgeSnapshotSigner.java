package tech.kayys.wayang.knowledge.seal;

import java.security.PrivateKey;
import java.security.Signature;
import java.util.Objects;

public final class Ed25519KnowledgeSnapshotSigner implements KnowledgeSnapshotSigner {

    private final PrivateKey privateKey;
    private final String keyId;
    private final String keyVersion;

    public Ed25519KnowledgeSnapshotSigner(PrivateKey privateKey, String keyId, String keyVersion) {
        this.privateKey = Objects.requireNonNull(privateKey);
        this.keyId = Objects.requireNonNull(keyId);
        this.keyVersion = Objects.requireNonNull(keyVersion);
    }

    @Override
    public KnowledgeSnapshotSealAlgorithm algorithm() {
        return KnowledgeSnapshotSealAlgorithm.ED25519;
    }

    @Override
    public String keyId() {
        return keyId;
    }

    @Override
    public String keyVersion() {
        return keyVersion;
    }

    @Override
    public byte[] sign(byte[] payload) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(payload);
            return signature.sign();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create Ed25519 seal", e);
        }
    }
}
