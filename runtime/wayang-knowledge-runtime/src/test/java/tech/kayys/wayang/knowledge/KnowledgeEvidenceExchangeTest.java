package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.InMemoryKnowledgeEvidenceArtifactStore;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifact;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;
import tech.kayys.wayang.knowledge.snapshot.artifact.Sha256KnowledgeEvidenceArtifactAddressing;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceExchangeTest {

    @Test
    void testEvidenceExchangeAndLocalFirstResolution() {
        Sha256KnowledgeEvidenceArtifactAddressing addressing = new Sha256KnowledgeEvidenceArtifactAddressing();
        InMemoryKnowledgeEvidenceArtifactStore localStore = new InMemoryKnowledgeEvidenceArtifactStore(addressing);

        byte[] remoteContent = "Remote Evidence Content".getBytes(StandardCharsets.UTF_8);
        KnowledgeEvidenceArtifactId artifactId = addressing.identify(remoteContent);

        KnowledgeEvidenceExchangeEndpoint mockEndpoint = request -> {
            if (request.operation() == KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT) {
                return new KnowledgeEvidenceExchangeResponse(
                        true, request.operation(), request.artifactId(),
                        remoteContent, "text/plain", null, null, null, null, Map.of()
                );
            }
            return new KnowledgeEvidenceExchangeResponse(
                    false, request.operation(), request.artifactId(),
                    new byte[0], null, null, null, "NOT_FOUND", "Not found", Map.of()
            );
        };

        RemoteKnowledgeEvidenceResolver remoteResolver = new RemoteKnowledgeEvidenceResolver(mockEndpoint);
        DefaultKnowledgeEvidenceResolver resolver = new DefaultKnowledgeEvidenceResolver(localStore, remoteResolver);

        assertFalse(localStore.exists(artifactId));

        Optional<KnowledgeEvidenceArtifact> resolved = resolver.resolve(artifactId);
        assertTrue(resolved.isPresent());
        assertArrayEquals(remoteContent, resolved.get().content());

        // Now local store should have cached it
        assertTrue(localStore.exists(artifactId));
    }
}
