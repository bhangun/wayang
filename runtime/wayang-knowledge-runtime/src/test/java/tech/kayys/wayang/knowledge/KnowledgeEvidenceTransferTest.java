package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.transfer.*;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceTransferTest {

    @Test
    public void testTransferSessionAndStateMachine() {
        InMemoryKnowledgeEvidenceTransferSessionStore sessionStore =
                new InMemoryKnowledgeEvidenceTransferSessionStore();

        DefaultKnowledgeEvidenceTransferStateMachine stateMachine =
                new DefaultKnowledgeEvidenceTransferStateMachine();

        Instant now = Instant.now();
        KnowledgeEvidenceTransferSession session = new KnowledgeEvidenceTransferSession(
                "xfer-1",
                KnowledgeEvidenceTransferOperation.DOWNLOAD,
                "sess-1", "stream-1", "art-1", "res-1",
                0L, 512L, 1024L, 4L,
                KnowledgeEvidenceTransferState.CREATED,
                "fp-art", "fp-res", "merkle-root",
                now, now.plusSeconds(300), Map.of()
        );

        assertEquals(KnowledgeEvidenceTransferState.CREATED, session.state());
        assertDoesNotThrow(() -> stateMachine.transition(session, KnowledgeEvidenceTransferState.AUTHORIZING));

        sessionStore.create(session);
        var found = sessionStore.find("xfer-1");
        assertTrue(found.isPresent());
        assertEquals(512L, found.get().currentOffset());
    }

    @Test
    public void testTransferCheckpointStore() {
        InMemoryKnowledgeEvidenceTransferCheckpointStore checkpointStore =
                new InMemoryKnowledgeEvidenceTransferCheckpointStore();

        Instant now = Instant.now();
        KnowledgeEvidenceTransferCheckpoint cp = new KnowledgeEvidenceTransferCheckpoint(
                "xfer-1", "sess-1", "stream-1", "art-1", "res-1",
                1024L, 10L, "sha256:art", "sha256:res", "merkle-root", now, Map.of()
        );

        checkpointStore.save(cp);
        var found = checkpointStore.find("xfer-1");
        assertTrue(found.isPresent());
        assertEquals(1024L, found.get().offset());
    }
}
