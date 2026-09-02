package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeEvidenceArtifactStore
        implements KnowledgeEvidenceArtifactStore {

    private final KnowledgeEvidenceArtifactAddressing addressing;
    private final Map<KnowledgeEvidenceArtifactId, KnowledgeEvidenceArtifact> artifacts = new ConcurrentHashMap<>();

    public InMemoryKnowledgeEvidenceArtifactStore(KnowledgeEvidenceArtifactAddressing addressing) {
        this.addressing = addressing;
    }

    @Override
    public KnowledgeEvidenceArtifactId put(KnowledgeEvidenceArtifact artifact) {
        KnowledgeEvidenceArtifactId actual = addressing.identify(artifact.content());

        if (!actual.digest().equalsIgnoreCase(artifact.metadata().artifactId().digest())) {
            throw new IllegalArgumentException("Artifact content does not match artifact ID");
        }

        artifacts.compute(actual, (id, existing) -> {
            if (existing == null) {
                return artifact;
            }
            if (!java.util.Arrays.equals(existing.content(), artifact.content())) {
                throw new IllegalStateException("Artifact identity collision detected: " + id);
            }
            return existing;
        });

        return actual;
    }

    @Override
    public Optional<KnowledgeEvidenceArtifact> get(KnowledgeEvidenceArtifactId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(artifacts.get(id));
    }

    @Override
    public boolean exists(KnowledgeEvidenceArtifactId id) {
        if (id == null) {
            return false;
        }
        return artifacts.containsKey(id);
    }

    @Override
    public void delete(KnowledgeEvidenceArtifactId id) {
        if (id != null) {
            artifacts.remove(id);
        }
    }

    @Override
    public long size() {
        return artifacts.size();
    }

    @Override
    public void clear() {
        artifacts.clear();
    }
}
