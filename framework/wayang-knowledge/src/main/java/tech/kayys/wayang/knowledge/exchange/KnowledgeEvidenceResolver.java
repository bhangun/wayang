package tech.kayys.wayang.knowledge.exchange;

import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifact;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;

import java.util.Optional;

public interface KnowledgeEvidenceResolver {
    Optional<KnowledgeEvidenceArtifact> resolve(KnowledgeEvidenceArtifactId artifactId);
}
