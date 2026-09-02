package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.identity.*;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceExchangeIdentityTest {

    @Test
    public void testRuntimeIdentityEnrollmentAndFingerprint() {
        InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry registry =
                new InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry();

        DefaultKnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer canonicalizer =
                new DefaultKnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer();

        Sha256KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter fingerprinter =
                new Sha256KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter(canonicalizer);

        DefaultKnowledgeEvidenceExchangeRuntimeIdentityService identityService =
                new DefaultKnowledgeEvidenceExchangeRuntimeIdentityService(registry, fingerprinter);

        Instant now = Instant.now();

        var identity = identityService.create(
                "runtime-alpha",
                "1.0.0",
                "Wayang Agent Core",
                "AGENT",
                "org-kayys",
                "tenant-xyz",
                "key-alpha-pub",
                "v1",
                "anchor-corp-root",
                now,
                now.plusSeconds(86400)
        );

        assertNotNull(identity);
        assertEquals("runtime-alpha", identity.runtimeId());
        assertNotNull(identity.identityFingerprint());
        assertEquals(KnowledgeEvidenceExchangeRuntimeIdentityStatus.PENDING, identity.status());

        registry.register(identity);
        var found = registry.find("runtime-alpha", "1.0.0");
        assertTrue(found.isPresent());
        assertEquals(identity.identityFingerprint(), found.get().identityFingerprint());
    }

    @Test
    public void testMutualTrustEvaluation() {
        InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry registry =
                new InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry();

        DefaultKnowledgeEvidenceExchangeRuntimeTrustService trustService =
                new DefaultKnowledgeEvidenceExchangeRuntimeTrustService(registry, "policy-default");

        DefaultKnowledgeEvidenceExchangeRuntimeMutualTrustService mutualTrustService =
                new DefaultKnowledgeEvidenceExchangeRuntimeMutualTrustService(trustService, "tenant-omega");

        Instant now = Instant.now();

        KnowledgeEvidenceExchangeRuntimeIdentity local = new KnowledgeEvidenceExchangeRuntimeIdentity(
                "runtime-local",
                "1.0",
                "Wayang Core Local",
                "RUNTIME",
                "org-kayys",
                "tenant-omega",
                now.minusSeconds(100),
                now.minusSeconds(100),
                now.plusSeconds(3600),
                KnowledgeEvidenceExchangeRuntimeIdentityStatus.ACTIVE,
                "fp-local",
                "key-local",
                "v1",
                "anchor-shared",
                Map.of()
        );

        KnowledgeEvidenceExchangeRuntimeIdentity peer = new KnowledgeEvidenceExchangeRuntimeIdentity(
                "runtime-peer",
                "1.0",
                "Wayang Core Peer",
                "RUNTIME",
                "org-kayys",
                "tenant-omega",
                now.minusSeconds(100),
                now.minusSeconds(100),
                now.plusSeconds(3600),
                KnowledgeEvidenceExchangeRuntimeIdentityStatus.ACTIVE,
                "fp-peer",
                "key-peer",
                "v1",
                "anchor-shared",
                Map.of()
        );

        registry.register(local);
        registry.register(peer);

        var handshake = mutualTrustService.establish(
                local, peer, "key-local", "v1", "key-peer", "v1", "nonce-loc", "nonce-rem", now
        );
        assertNotNull(handshake);
        assertTrue(handshake.successful());
        assertEquals(KnowledgeEvidenceExchangeRuntimeHandshakeStatus.MUTUALLY_TRUSTED, handshake.status());
    }
}
