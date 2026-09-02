package tech.kayys.wayang.knowledge.seal;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;
import java.util.Objects;

public final class Ed25519KnowledgeSnapshotSignatureVerifier implements KnowledgeSnapshotSignatureVerifier {

    private final Map<String, PublicKey> keys;

    public Ed25519KnowledgeSnapshotSignatureVerifier(Map<String, PublicKey> keys) {
        this.keys = Map.copyOf(Objects.requireNonNull(keys));
    }

    @Override
    public boolean supports(KnowledgeSnapshotSealAlgorithm algorithm) {
        return algorithm == KnowledgeSnapshotSealAlgorithm.ED25519;
    }

    @Override
    public boolean verify(byte[] payload, byte[] signatureBytes, String keyId, String keyVersion) {
        try {
            PublicKey key = keys.get(keyId + ":" + keyVersion);
            if (key == null) {
                key = keys.get(keyId);
            }
            if (key == null) {
                return false;
            }

            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(key);
            signature.update(payload);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
