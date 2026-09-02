package tech.kayys.wayang.knowledge.exchange;

import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifact;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactMetadata;
import tech.kayys.wayang.knowledge.snapshot.merkle.KnowledgeEvidenceMerkleProof;
import tech.kayys.wayang.knowledge.snapshot.pack.KnowledgeSnapshotVerificationManifest;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class RemoteKnowledgeEvidenceResolver
        implements KnowledgeEvidenceRemoteResolver {

    private final KnowledgeEvidenceExchangeEndpoint endpoint;

    public RemoteKnowledgeEvidenceResolver(KnowledgeEvidenceExchangeEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Optional<KnowledgeEvidenceArtifact> resolve(KnowledgeEvidenceArtifactId artifactId) {
        KnowledgeEvidenceExchangeResponse response = endpoint.exchange(
                KnowledgeEvidenceExchangeRequest.resolve(artifactId)
        );

        if (!response.success()) {
            return Optional.empty();
        }

        return Optional.of(
                new KnowledgeEvidenceArtifact(
                        new KnowledgeEvidenceArtifactMetadata(
                                artifactId,
                                response.mediaType() != null ? response.mediaType() : "application/octet-stream",
                                response.content().length,
                                Instant.now(),
                                "remote",
                                "1",
                                Map.of()
                        ),
                        response.content()
                )
        );
    }

    @Override
    public Optional<KnowledgeSnapshotVerificationManifest> manifest(KnowledgeEvidenceArtifactId artifactId) {
        KnowledgeEvidenceExchangeResponse response = endpoint.exchange(
                new KnowledgeEvidenceExchangeRequest(
                        KnowledgeEvidenceExchangeOperation.GET_MANIFEST,
                        artifactId,
                        null,
                        null,
                        null,
                        false,
                        true,
                        true,
                        Map.of()
                )
        );

        return response.success() ? Optional.ofNullable(response.manifest()) : Optional.empty();
    }

    @Override
    public Optional<KnowledgeEvidenceMerkleProof> proof(KnowledgeEvidenceArtifactId artifactId, String leafId) {
        KnowledgeEvidenceExchangeResponse response = endpoint.exchange(
                KnowledgeEvidenceExchangeRequest.proof(artifactId, leafId)
        );

        return response.success() ? Optional.ofNullable(response.merkleProof()) : Optional.empty();
    }
}
