package tech.kayys.wayang.knowledge.exchange;

import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifact;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactStore;

import java.util.Optional;

public final class DefaultKnowledgeEvidenceResolver
        implements KnowledgeEvidenceResolver {

    private final KnowledgeEvidenceArtifactStore localStore;
    private final KnowledgeEvidenceRemoteResolver remoteResolver;

    public DefaultKnowledgeEvidenceResolver(
            KnowledgeEvidenceArtifactStore localStore,
            KnowledgeEvidenceRemoteResolver remoteResolver
    ) {
        this.localStore = localStore;
        this.remoteResolver = remoteResolver;
    }

    @Override
    public Optional<KnowledgeEvidenceArtifact> resolve(KnowledgeEvidenceArtifactId artifactId) {
        if (artifactId == null) {
            return Optional.empty();
        }

        // Local-first check
        Optional<KnowledgeEvidenceArtifact> local = localStore.get(artifactId);
        if (local.isPresent()) {
            return local;
        }

        if (remoteResolver == null) {
            return Optional.empty();
        }

        // Remote fallback & cache locally
        Optional<KnowledgeEvidenceArtifact> remote = remoteResolver.resolve(artifactId);
        remote.ifPresent(localStore::put);

        return remote;
    }
}
