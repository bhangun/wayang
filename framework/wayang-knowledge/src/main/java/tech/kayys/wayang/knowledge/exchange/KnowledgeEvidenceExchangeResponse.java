package tech.kayys.wayang.knowledge.exchange;

import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;
import tech.kayys.wayang.knowledge.snapshot.merkle.KnowledgeEvidenceMerkleProof;
import tech.kayys.wayang.knowledge.snapshot.pack.KnowledgeSnapshotVerificationManifest;

import java.util.Map;

public record KnowledgeEvidenceExchangeResponse(
        boolean success,
        KnowledgeEvidenceExchangeOperation operation,
        KnowledgeEvidenceArtifactId artifactId,
        byte[] content,
        String mediaType,
        KnowledgeSnapshotVerificationManifest manifest,
        KnowledgeEvidenceMerkleProof merkleProof,
        String errorCode,
        String errorMessage,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeResponse {
        content = content == null ? new byte[0] : content.clone();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
