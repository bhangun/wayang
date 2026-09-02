package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class DefaultKnowledgeEvidenceArtifactService
        implements KnowledgeEvidenceArtifactService {

    private final KnowledgeEvidenceArtifactAddressing addressing;
    private final KnowledgeEvidenceArtifactStore store;

    public DefaultKnowledgeEvidenceArtifactService(
            KnowledgeEvidenceArtifactAddressing addressing,
            KnowledgeEvidenceArtifactStore store
    ) {
        this.addressing = addressing;
        this.store = store;
    }

    @Override
    public KnowledgeEvidenceArtifactPutResult put(
            byte[] content,
            String mediaType,
            String producer,
            String schemaVersion
    ) {
        if (content == null) {
            content = new byte[0];
        }

        KnowledgeEvidenceArtifactId id = addressing.identify(content);
        boolean existed = store.exists(id);

        KnowledgeEvidenceArtifactMetadata metadata = new KnowledgeEvidenceArtifactMetadata(
                id,
                mediaType != null ? mediaType : "application/octet-stream",
                content.length,
                Instant.now(),
                producer != null ? producer : "wayang",
                schemaVersion != null ? schemaVersion : "1",
                Map.of()
        );

        KnowledgeEvidenceArtifact artifact = new KnowledgeEvidenceArtifact(metadata, content);
        store.put(artifact);

        return new KnowledgeEvidenceArtifactPutResult(
                id,
                !existed,
                existed,
                content.length
        );
    }

    @Override
    public Optional<KnowledgeEvidenceArtifact> get(KnowledgeEvidenceArtifactId id) {
        if (id == null) {
            return Optional.empty();
        }
        return store.get(id);
    }

    @Override
    public boolean verify(KnowledgeEvidenceArtifactId id) {
        if (id == null) {
            return false;
        }
        Optional<KnowledgeEvidenceArtifact> artifact = store.get(id);
        if (artifact.isEmpty()) {
            return false;
        }
        return addressing.matches(id, artifact.get().content());
    }
}
