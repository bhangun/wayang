package tech.kayys.wayang.knowledge.snapshot.artifact;

public interface KnowledgeEvidenceArtifactAddressing {
    KnowledgeEvidenceArtifactId identify(byte[] content);
    boolean matches(KnowledgeEvidenceArtifactId id, byte[] content);
}
