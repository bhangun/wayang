package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.security.MessageDigest;

public final class Sha256KnowledgeEvidenceArtifactAddressing
        implements KnowledgeEvidenceArtifactAddressing {

    @Override
    public KnowledgeEvidenceArtifactId identify(byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("content is required");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return new KnowledgeEvidenceArtifactId("sha256", hex.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate SHA-256", e);
        }
    }

    @Override
    public boolean matches(KnowledgeEvidenceArtifactId id, byte[] content) {
        if (id == null || content == null) {
            return false;
        }
        return identify(content).digest().equalsIgnoreCase(id.digest());
    }
}
