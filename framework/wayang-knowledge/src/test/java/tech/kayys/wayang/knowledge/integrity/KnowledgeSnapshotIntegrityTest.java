package tech.kayys.wayang.knowledge.integrity;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.dependency.InMemoryKnowledgeSnapshotDependencyGraph;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSnapshotIntegrityTest {

    @Test
    void testIntegrityVerification() {
        KnowledgeIntegrityFingerprintProvider fingerprints = new DefaultKnowledgeIntegrityFingerprintProvider();
        InMemoryKnowledgeSnapshotDependencyGraph graph = new InMemoryKnowledgeSnapshotDependencyGraph();
        KnowledgeSnapshotIntegrityPolicy policy = KnowledgeSnapshotIntegrityPolicy.strict();

        DefaultKnowledgeSnapshotIntegrityVerifier verifier = new DefaultKnowledgeSnapshotIntegrityVerifier(
                fingerprints, graph, policy
        );

        String kFingerprint = fingerprints.fingerprintKnowledge("doc-1", "v1");
        String pFingerprint = fingerprints.fingerprintPolicy("policy-1", "v1");

        KnowledgeSnapshotId snapId = KnowledgeSnapshotId.of("snap-integ-1");
        KnowledgeDecisionSnapshot validSnapshot = new KnowledgeDecisionSnapshot(
                snapId,
                "exec-1", "trace-1", "agent-1", "op", "query", Instant.now(),
                List.of(new KnowledgeSnapshotEntry("doc-1", "v1", kFingerprint, "prov-1", "auth-1", "trust-1", "lin-1", java.util.Map.of())),
                new KnowledgePolicySnapshot(List.of(new KnowledgeVersionReference("policy-1", "v1", "policy", pFingerprint, java.util.Map.of())), "p-agg", java.util.Map.of()),
                new KnowledgeRuleSnapshot(List.of(), "", java.util.Map.of()),
                new KnowledgeGovernanceSnapshot(null, null, null, null, null, null, null, java.util.Map.of()),
                new KnowledgeRuntimeSnapshot(null, null, null, null, null, null, null, null, java.util.Map.of()),
                "agg-fp", Instant.now(), java.util.Map.of()
        );

        KnowledgeSnapshotIntegrityResult result = verifier.verify(validSnapshot);
        assertTrue(result.isValid());
        assertEquals(KnowledgeSnapshotIntegrityStatus.ATTESTED, result.status());
        assertTrue(result.mismatches().isEmpty());

        // Tampered snapshot with bad fingerprint
        KnowledgeDecisionSnapshot tamperedSnapshot = new KnowledgeDecisionSnapshot(
                snapId,
                "exec-1", "trace-1", "agent-1", "op", "query", Instant.now(),
                List.of(new KnowledgeSnapshotEntry("doc-1", "v1", "sha256:corrupted", "prov-1", "auth-1", "trust-1", "lin-1", java.util.Map.of())),
                validSnapshot.policies(),
                validSnapshot.rules(),
                validSnapshot.governance(),
                validSnapshot.runtime(),
                "agg-fp", Instant.now(), java.util.Map.of()
        );

        KnowledgeSnapshotIntegrityResult tamperedResult = verifier.verify(tamperedSnapshot);
        assertFalse(tamperedResult.isValid());
        assertEquals(KnowledgeSnapshotIntegrityStatus.TAMPERED, tamperedResult.status());
        assertEquals(1, tamperedResult.mismatches().size());
        assertEquals(KnowledgeSnapshotIntegrityMismatchType.KNOWLEDGE, tamperedResult.mismatches().get(0).type());
    }
}
