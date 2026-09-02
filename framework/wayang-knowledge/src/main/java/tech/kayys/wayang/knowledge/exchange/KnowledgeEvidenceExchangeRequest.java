package tech.kayys.wayang.knowledge.exchange;

import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;

import java.util.Map;

public record KnowledgeEvidenceExchangeRequest(
        KnowledgeEvidenceExchangeOperation operation,
        KnowledgeEvidenceArtifactId artifactId,
        String resourceId,
        String snapshotId,
        String leafId,
        boolean requireProof,
        boolean requireManifest,
        boolean allowRemoteFetch,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeRequest {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeEvidenceExchangeRequest resolve(
            KnowledgeEvidenceArtifactId artifactId
    ) {
        return new KnowledgeEvidenceExchangeRequest(
                KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT,
                artifactId,
                null,
                null,
                null,
                false,
                false,
                true,
                Map.of()
        );
    }

    public static KnowledgeEvidenceExchangeRequest resource(
            KnowledgeEvidenceArtifactId artifactId,
            String resourceId
    ) {
        return new KnowledgeEvidenceExchangeRequest(
                KnowledgeEvidenceExchangeOperation.GET_RESOURCE,
                artifactId,
                resourceId,
                null,
                null,
                false,
                true,
                true,
                Map.of()
        );
    }

    public static KnowledgeEvidenceExchangeRequest proof(
            KnowledgeEvidenceArtifactId artifactId,
            String leafId
    ) {
        return new KnowledgeEvidenceExchangeRequest(
                KnowledgeEvidenceExchangeOperation.GET_MERKLE_PROOF,
                artifactId,
                null,
                null,
                leafId,
                true,
                true,
                true,
                Map.of()
        );
    }
}
