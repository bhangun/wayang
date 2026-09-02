package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class DefaultKnowledgeEvidenceMerkleProofVerifier
        implements KnowledgeEvidenceMerkleProofVerifier {

    @Override
    public boolean verify(KnowledgeEvidenceMerkleProof proof) {
        if (proof == null || proof.leafHash() == null || proof.rootHash() == null) {
            return false;
        }

        String current = proof.leafHash();

        for (KnowledgeEvidenceMerkleProofStep step : proof.steps()) {
            current = switch (step.direction()) {
                case LEFT -> hashPair(step.siblingHash(), current);
                case RIGHT -> hashPair(current, step.siblingHash());
            };
        }

        return current.equals(proof.rootHash());
    }

    private String hashPair(String left, String right) {
        return sha256("node|" + left + "|" + right);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate hash", e);
        }
    }
}
