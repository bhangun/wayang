package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;
import tech.kayys.wayang.knowledge.exchange.session.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceExchangeSessionTest {

    @Test
    void testSessionAndReplayGuard() {
        InMemoryKnowledgeEvidenceExchangeSessionStore sessionStore = new InMemoryKnowledgeEvidenceExchangeSessionStore();
        DefaultKnowledgeEvidenceExchangeSessionService sessionService =
                new DefaultKnowledgeEvidenceExchangeSessionService(sessionStore);

        Instant now = Instant.now();
        KnowledgeEvidenceExchangeSession session = sessionService.createSession(
                "runtime-local", "runtime-remote", "principal-1",
                "tenant-1", "ws-1", "proj-1", Duration.ofMinutes(30), now
        );

        assertNotNull(session);
        assertEquals(KnowledgeEvidenceExchangeSessionStatus.CREATED, session.status());

        KnowledgeEvidenceExchangeSession established = sessionService.establish(session.sessionId(), "remote-nonce-xyz", now);
        assertEquals(KnowledgeEvidenceExchangeSessionStatus.ESTABLISHED, established.status());

        InMemoryKnowledgeEvidenceExchangeReplayGuard replayGuard = new InMemoryKnowledgeEvidenceExchangeReplayGuard();
        Sha256KnowledgeEvidenceExchangeBindingFingerprinter fingerprinter =
                new Sha256KnowledgeEvidenceExchangeBindingFingerprinter(new DefaultKnowledgeEvidenceExchangeBindingCanonicalizer());

        KnowledgeEvidenceExchangeRequestBinding binding = new KnowledgeEvidenceExchangeRequestBinding(
                "req-1", session.sessionId(), "nonce-1", "runtime-local", "runtime-remote",
                "tenant-1", "ws-1", "proj-1", KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT,
                "sha256:abc", null, now, now.plus(Duration.ofMinutes(5)), "corr-1", "fingerprint-1", Map.of()
        );

        KnowledgeEvidenceExchangeReplayStatus status1 = replayGuard.checkAndRecord(binding, "principal-1", now);
        assertEquals(KnowledgeEvidenceExchangeReplayStatus.ACCEPTED, status1);

        // Replay attempt with same request ID and nonce
        KnowledgeEvidenceExchangeReplayStatus status2 = replayGuard.checkAndRecord(binding, "principal-1", now);
        assertEquals(KnowledgeEvidenceExchangeReplayStatus.REPLAYED, status2);
    }
}
