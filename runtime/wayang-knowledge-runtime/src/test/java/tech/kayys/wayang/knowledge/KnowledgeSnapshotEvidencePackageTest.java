package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionOutcome;
import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionRecorder;
import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;
import tech.kayys.wayang.knowledge.integrity.DefaultKnowledgeIntegrityFingerprintProvider;
import tech.kayys.wayang.knowledge.integrity.DefaultKnowledgeSnapshotIntegrityVerifier;
import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityPolicy;
import tech.kayys.wayang.knowledge.seal.DefaultKnowledgeSnapshotSealCanonicalizer;
import tech.kayys.wayang.knowledge.seal.DefaultKnowledgeSnapshotSealVerificationService;
import tech.kayys.wayang.knowledge.seal.Ed25519KnowledgeSnapshotSignatureVerifier;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.dependency.InMemoryKnowledgeSnapshotDependencyGraph;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.DefaultKnowledgeSnapshotRegistry;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.InMemoryKnowledgeSnapshotRegistryStore;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.KnowledgeSnapshotRetentionPolicy;
import tech.kayys.wayang.knowledge.snapshot.pack.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSnapshotEvidencePackageTest {

    @Test
    void testCreateAndVerifyEvidencePackage() {
        KnowledgeSnapshotCanonicalizer canonicalizer = new DefaultKnowledgeSnapshotCanonicalizer();
        KnowledgeDecisionSnapshotFactory factory = new KnowledgeDecisionSnapshotFactory(canonicalizer);
        InMemoryKnowledgeDecisionSnapshotStore store = new InMemoryKnowledgeDecisionSnapshotStore();
        DefaultKnowledgeDecisionSnapshotService snapshotService =
                new DefaultKnowledgeDecisionSnapshotService(factory, store);

        KnowledgeDecisionTrace trace = new KnowledgeDecisionRecorder("trace-ev-1")
                .executionId("exec-1")
                .agentId("agent-1")
                .operation("EVAL")
                .outcome(KnowledgeDecisionOutcome.allowed("OK", "Allowed"))
                .build();

        KnowledgeSnapshotCaptureContext context = new KnowledgeSnapshotCaptureContext(
                List.of(new KnowledgeSnapshotEntry("k-1", "v1", "fp-1", "prov-1", "auth-1", "trust-1", "lin-1", Map.of())),
                new KnowledgePolicySnapshot(List.of(), "p-agg", Map.of()),
                new KnowledgeRuleSnapshot(List.of(), "r-agg", Map.of()),
                new KnowledgeGovernanceSnapshot("tenant-1", "ws-1", "proj-1", "user-1", "2026-09-02T00:00:00Z", "scope-fp", "gov-fp", Map.of()),
                new KnowledgeRuntimeSnapshot("1.0", "1.0", "1.0", "1.0", "1.0", "openai", "gpt-4", "latest", Map.of())
        );

        KnowledgeDecisionSnapshot snapshot = snapshotService.capture(trace, context);

        DefaultKnowledgeSnapshotVerificationManifestFactory manifestFactory =
                new DefaultKnowledgeSnapshotVerificationManifestFactory();
        KnowledgeSnapshotVerificationManifest manifest = manifestFactory.create(snapshot, null, null);

        assertNotNull(manifest);
        assertEquals(snapshot.snapshotId().value(), manifest.snapshotId());

        KnowledgeSnapshotPackageResource resource = new KnowledgeSnapshotPackageResource(
                "res-1", "rule", "1",
                KnowledgeSnapshotPackageFingerprint.sha256("policy content"),
                "application/json",
                "policy content".getBytes(StandardCharsets.UTF_8),
                Map.of()
        );

        KnowledgeSnapshotEvidencePackage pkg = new KnowledgeSnapshotEvidencePackageBuilder()
                .packageId("pkg-123")
                .manifest(manifest)
                .resource(resource)
                .build();

        DefaultKnowledgeSnapshotIntegrityVerifier integrityVerifier = new DefaultKnowledgeSnapshotIntegrityVerifier(
                new DefaultKnowledgeIntegrityFingerprintProvider(),
                new InMemoryKnowledgeSnapshotDependencyGraph(),
                KnowledgeSnapshotIntegrityPolicy.strict()
        );

        DefaultKnowledgeSnapshotRegistry registry = new DefaultKnowledgeSnapshotRegistry(
                new InMemoryKnowledgeSnapshotRegistryStore(),
                KnowledgeSnapshotRetentionPolicy.defaults()
        );

        DefaultKnowledgeSnapshotEvidencePackageVerifier verifier =
                new DefaultKnowledgeSnapshotEvidencePackageVerifier(
                        new tech.kayys.wayang.knowledge.integrity.DefaultKnowledgeSnapshotIntegrityService(
                                registry,
                                integrityVerifier
                        ),
                        new DefaultKnowledgeSnapshotSealVerificationService(
                                new DefaultKnowledgeSnapshotSealCanonicalizer(),
                                new Ed25519KnowledgeSnapshotSignatureVerifier(Map.of())
                        )
                );

        KnowledgeSnapshotPackageVerificationResult result =
                verifier.verify(pkg, KnowledgeSnapshotPackageVerificationContext.relaxed());

        assertNotNull(result);
        assertTrue(result.verified());
        assertEquals("pkg-123", result.packageId());
    }
}
