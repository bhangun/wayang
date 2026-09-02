package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.envelope.KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm;
import tech.kayys.wayang.knowledge.exchange.trust.*;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceExchangeTrustTest {

    @Test
    public void testKeyTrustRegistrationAndRotation() {
        InMemoryKnowledgeEvidenceExchangeKeyTrustRegistry registry = new InMemoryKnowledgeEvidenceExchangeKeyTrustRegistry();
        Instant now = Instant.now();

        KnowledgeEvidenceExchangeTrustedKey keyV1 = new KnowledgeEvidenceExchangeTrustedKey(
                "key-1",
                "v1",
                KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm.ED25519,
                "runtime-1",
                "tenant-alpha",
                now.minusSeconds(3600),
                now.plusSeconds(3600),
                true,
                false,
                "anchor-root",
                Map.of()
        );

        registry.register(keyV1);
        assertTrue(registry.isTrusted("key-1", "v1", now));

        DefaultKnowledgeEvidenceExchangeKeyRotationService rotationService =
                new DefaultKnowledgeEvidenceExchangeKeyRotationService(registry);

        KnowledgeEvidenceExchangeTrustedKey keyV2 = new KnowledgeEvidenceExchangeTrustedKey(
                "key-1",
                "v2",
                KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm.ED25519,
                "runtime-1",
                "tenant-alpha",
                now,
                now.plusSeconds(7200),
                true,
                false,
                "anchor-root",
                Map.of()
        );

        var rotation = rotationService.rotate(keyV1, keyV2, now, "Routine scheduled rotation");
        assertNotNull(rotation);
        assertEquals("key-1", rotation.keyId());
        assertEquals("v1", rotation.previousVersion());
        assertEquals("v2", rotation.newVersion());

        assertTrue(registry.isTrusted("key-1", "v2", now));
    }

    @Test
    public void testKeyRevocation() {
        InMemoryKnowledgeEvidenceExchangeKeyTrustRegistry registry = new InMemoryKnowledgeEvidenceExchangeKeyTrustRegistry();
        Instant now = Instant.now();

        KnowledgeEvidenceExchangeTrustedKey key = new KnowledgeEvidenceExchangeTrustedKey(
                "key-compromised",
                "v1",
                KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm.HMAC_SHA256,
                "runtime-2",
                "tenant-beta",
                now.minusSeconds(100),
                now.plusSeconds(500),
                true,
                false,
                "anchor-beta",
                Map.of()
        );

        registry.register(key);
        assertTrue(registry.isTrusted("key-compromised", "v1", now));

        registry.revoke("key-compromised", "v1", "Compromised credential");
        assertFalse(registry.isTrusted("key-compromised", "v1", now));
        assertEquals(KnowledgeEvidenceExchangeKeyTrustStatus.REVOKED, registry.find("key-compromised", "v1").get().trustStatus(now));
    }
}
