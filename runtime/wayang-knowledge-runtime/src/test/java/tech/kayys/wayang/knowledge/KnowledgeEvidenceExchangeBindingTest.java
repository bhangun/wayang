package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeResponse;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.session.KnowledgeEvidenceExchangeRequestBinding;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceExchangeBindingTest {

    @Test
    void testResponseBindingAndVerification() {
        DefaultKnowledgeEvidenceExchangeResponseBindingCanonicalizer canonicalizer =
                new DefaultKnowledgeEvidenceExchangeResponseBindingCanonicalizer();
        Sha256KnowledgeEvidenceExchangeResponseFingerprinter fingerprinter =
                new Sha256KnowledgeEvidenceExchangeResponseFingerprinter(canonicalizer);
        UuidKnowledgeEvidenceExchangeResponseIdGenerator idGen =
                new UuidKnowledgeEvidenceExchangeResponseIdGenerator();

        KnowledgeEvidenceExchangeResponseBindingFactory factory =
                new KnowledgeEvidenceExchangeResponseBindingFactory(fingerprinter, idGen);

        Instant now = Instant.now();
        KnowledgeEvidenceArtifactId artifactId = new KnowledgeEvidenceArtifactId("sha256", "12345");

        KnowledgeEvidenceExchangeRequestBinding requestBinding = new KnowledgeEvidenceExchangeRequestBinding(
                "req-1", "session-1", "nonce-1", "runtime-client", "runtime-server",
                "tenant-1", "ws-1", "proj-1", KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT,
                artifactId.value(), null, now, now.plus(Duration.ofMinutes(10)), "corr-1", "bind-fp", Map.of()
        );

        byte[] payload = "test-payload".getBytes(StandardCharsets.UTF_8);
        KnowledgeEvidenceExchangeResponse response = new KnowledgeEvidenceExchangeResponse(
                true, KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT, artifactId,
                payload, "text/plain", null, null, null, null, Map.of()
        );

        KnowledgeEvidenceExchangeResponseBinding responseBinding = factory.create(
                requestBinding, response, "runtime-server", "runtime-client", Duration.ofMinutes(10), now
        );

        DefaultKnowledgeEvidenceExchangeResponseVerifier verifier =
                new DefaultKnowledgeEvidenceExchangeResponseVerifier(fingerprinter);

        KnowledgeEvidenceExchangeResponseVerificationResult result = verifier.verify(
                requestBinding, responseBinding, response, "runtime-server", now
        );

        assertNotNull(result);
        assertTrue(result.valid());
        assertEquals(KnowledgeEvidenceExchangeResponseVerificationStatus.VALID, result.status());
    }
}
