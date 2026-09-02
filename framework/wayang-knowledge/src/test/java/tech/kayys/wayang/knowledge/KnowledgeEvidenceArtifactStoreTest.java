package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceArtifactStoreTest {

    @Test
    void testArtifactDeduplicationAndVerification() {
        Sha256KnowledgeEvidenceArtifactAddressing addressing = new Sha256KnowledgeEvidenceArtifactAddressing();
        InMemoryKnowledgeEvidenceArtifactStore store = new InMemoryKnowledgeEvidenceArtifactStore(addressing);
        DefaultKnowledgeEvidenceArtifactService service = new DefaultKnowledgeEvidenceArtifactService(addressing, store);

        byte[] content = "Hello Wayang Artifact".getBytes(StandardCharsets.UTF_8);

        KnowledgeEvidenceArtifactPutResult result1 = service.put(content, "text/plain", "producer-1", "1");
        assertTrue(result1.created());
        assertFalse(result1.deduplicated());

        // Second put with same content should be deduplicated
        KnowledgeEvidenceArtifactPutResult result2 = service.put(content, "text/plain", "producer-2", "1");
        assertFalse(result2.created());
        assertTrue(result2.deduplicated());
        assertEquals(result1.artifactId(), result2.artifactId());

        Optional<KnowledgeEvidenceArtifact> fetched = service.get(result1.artifactId());
        assertTrue(fetched.isPresent());
        assertArrayEquals(content, fetched.get().content());

        assertTrue(service.verify(result1.artifactId()));
    }
}
