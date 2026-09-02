package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.util.Optional;

public interface KnowledgeEvidenceArtifactStore {
    KnowledgeEvidenceArtifactId put(KnowledgeEvidenceArtifact artifact);
    Optional<KnowledgeEvidenceArtifact> get(KnowledgeEvidenceArtifactId id);
    boolean exists(KnowledgeEvidenceArtifactId id);
    void delete(KnowledgeEvidenceArtifactId id);
    long size();
    void clear();
}
