package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.util.Optional;

public interface KnowledgeEvidenceArtifactService {

    KnowledgeEvidenceArtifactPutResult put(
            byte[] content,
            String mediaType,
            String producer,
            String schemaVersion
    );

    Optional<KnowledgeEvidenceArtifact> get(
            KnowledgeEvidenceArtifactId id
    );

    boolean verify(
            KnowledgeEvidenceArtifactId id
    );
}
