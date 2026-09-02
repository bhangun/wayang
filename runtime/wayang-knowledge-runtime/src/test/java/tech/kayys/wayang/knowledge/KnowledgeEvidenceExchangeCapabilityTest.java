package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.identity.InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry;
import tech.kayys.wayang.knowledge.exchange.identity.KnowledgeEvidenceExchangeRuntimeIdentity;
import tech.kayys.wayang.knowledge.exchange.identity.KnowledgeEvidenceExchangeRuntimeIdentityStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceExchangeCapabilityTest {

    @Test
    public void testCapabilityManifestAndNegotiation() {
        InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry identityRegistry =
                new InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry();

        DefaultKnowledgeEvidenceExchangeCapabilityManifestCanonicalizer canonicalizer =
                new DefaultKnowledgeEvidenceExchangeCapabilityManifestCanonicalizer();

        Sha256KnowledgeEvidenceExchangeCapabilityManifestFingerprinter fingerprinter =
                new Sha256KnowledgeEvidenceExchangeCapabilityManifestFingerprinter(canonicalizer);

        Instant now = Instant.now();
        var v10 = new KnowledgeEvidenceExchangeProtocolVersion(1, 0);

        KnowledgeEvidenceExchangeRuntimeIdentity providerIdentity = new KnowledgeEvidenceExchangeRuntimeIdentity(
                "runtime-provider", "1.0", "Provider", "AGENT", "org-kayys", "tenant-alpha",
                now.minusSeconds(10), now.minusSeconds(10), now.plusSeconds(3600),
                KnowledgeEvidenceExchangeRuntimeIdentityStatus.ACTIVE, "fp-prov", "key-1", "v1", "anchor-1", Map.of()
        );
        identityRegistry.register(providerIdentity);

        KnowledgeEvidenceExchangeRuntimeCapability capResolve = new KnowledgeEvidenceExchangeRuntimeCapability(
                KnowledgeEvidenceExchangeCapabilityType.ARTIFACT_RESOLUTION,
                true,
                Set.of("ED25519"),
                Set.of("application/octet-stream"),
                10 * 1024 * 1024L,
                true,
                Map.of("format", "binary")
        );

        KnowledgeEvidenceExchangeRuntimeCapability capMerkle = new KnowledgeEvidenceExchangeRuntimeCapability(
                KnowledgeEvidenceExchangeCapabilityType.MERKLE_PROOF,
                true,
                Set.of("SHA-256"),
                Set.of("application/json"),
                1024 * 1024L,
                true,
                Map.of()
        );

        DefaultKnowledgeEvidenceExchangeCapabilityDiscoveryService discoveryService =
                new DefaultKnowledgeEvidenceExchangeCapabilityDiscoveryService(
                        identityRegistry, fingerprinter, v10, List.of(capResolve, capMerkle)
                );

        var manifest = discoveryService.discover("runtime-provider", now);
        assertNotNull(manifest);
        assertEquals(2, manifest.capabilities().size());
        assertNotNull(manifest.manifestFingerprint());

        DefaultKnowledgeEvidenceExchangeCapabilityNegotiator negotiator =
                new DefaultKnowledgeEvidenceExchangeCapabilityNegotiator();

        DefaultKnowledgeEvidenceExchangeCapabilityNegotiationService negotiationService =
                new DefaultKnowledgeEvidenceExchangeCapabilityNegotiationService(negotiator);

        var request = new KnowledgeEvidenceExchangeCapabilityNegotiationRequest(
                "runtime-client",
                "runtime-provider",
                manifest,
                manifest,
                now,
                Map.of()
        );

        var result = negotiationService.negotiate(request);
        assertNotNull(result);
        assertTrue(result.successful());
        assertTrue(negotiationService.isAllowed(result, KnowledgeEvidenceExchangeCapabilityType.ARTIFACT_RESOLUTION));
    }
}
