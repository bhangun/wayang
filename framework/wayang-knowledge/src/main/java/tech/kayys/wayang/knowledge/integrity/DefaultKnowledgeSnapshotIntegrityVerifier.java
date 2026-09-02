package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotEntry;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeVersionReference;
import tech.kayys.wayang.knowledge.snapshot.dependency.KnowledgeSnapshotDependency;
import tech.kayys.wayang.knowledge.snapshot.dependency.KnowledgeSnapshotDependencyGraph;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeSnapshotIntegrityVerifier implements KnowledgeSnapshotIntegrityVerifier {

    private final KnowledgeIntegrityFingerprintProvider fingerprints;
    private final KnowledgeSnapshotDependencyGraph dependencyGraph;
    private final KnowledgeSnapshotIntegrityPolicy policy;

    public DefaultKnowledgeSnapshotIntegrityVerifier(
            KnowledgeIntegrityFingerprintProvider fingerprints,
            KnowledgeSnapshotDependencyGraph dependencyGraph,
            KnowledgeSnapshotIntegrityPolicy policy) {

        this.fingerprints = Objects.requireNonNull(fingerprints, "fingerprints is required");
        this.dependencyGraph = Objects.requireNonNull(dependencyGraph, "dependencyGraph is required");
        this.policy = policy != null ? policy : KnowledgeSnapshotIntegrityPolicy.strict();
    }

    @Override
    public KnowledgeSnapshotIntegrityResult verify(KnowledgeDecisionSnapshot snapshot) {
        List<KnowledgeSnapshotIntegrityMismatch> mismatches = new ArrayList<>();
        List<String> verified = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        if (policy.verifyKnowledge()) {
            for (KnowledgeSnapshotEntry entry : snapshot.knowledge()) {
                if (entry.fingerprint() == null || entry.fingerprint().isBlank()) {
                    missing.add("knowledge:" + entry.knowledgeId());
                    continue;
                }
                String actual = fingerprints.fingerprintKnowledge(entry.knowledgeId(), entry.versionId());
                if (!entry.fingerprint().equals(actual)) {
                    mismatches.add(new KnowledgeSnapshotIntegrityMismatch(
                            KnowledgeSnapshotIntegrityMismatchType.KNOWLEDGE,
                            entry.knowledgeId(),
                            entry.fingerprint(),
                            actual,
                            "Knowledge fingerprint mismatch",
                            java.util.Map.of("versionId", entry.versionId())
                    ));
                } else {
                    verified.add("knowledge:" + entry.knowledgeId());
                }
            }
        }

        if (policy.verifyPolicies()) {
            for (KnowledgeVersionReference ref : snapshot.policies().references()) {
                String actual = fingerprints.fingerprintPolicy(ref.id(), ref.versionId());
                if (!ref.fingerprint().equals(actual)) {
                    mismatches.add(new KnowledgeSnapshotIntegrityMismatch(
                            KnowledgeSnapshotIntegrityMismatchType.POLICY,
                            ref.id(),
                            ref.fingerprint(),
                            actual,
                            "Policy fingerprint mismatch",
                            java.util.Map.of("versionId", ref.versionId())
                    ));
                } else {
                    verified.add("policy:" + ref.id());
                }
            }
        }

        if (policy.verifyRules()) {
            for (KnowledgeVersionReference ref : snapshot.rules().references()) {
                String actual = fingerprints.fingerprintRule(ref.id(), ref.versionId());
                if (!ref.fingerprint().equals(actual)) {
                    mismatches.add(new KnowledgeSnapshotIntegrityMismatch(
                            KnowledgeSnapshotIntegrityMismatchType.RULE,
                            ref.id(),
                            ref.fingerprint(),
                            actual,
                            "Rule fingerprint mismatch",
                            java.util.Map.of("versionId", ref.versionId())
                    ));
                } else {
                    verified.add("rule:" + ref.id());
                }
            }
        }

        if (policy.verifyDependencies()) {
            List<KnowledgeSnapshotDependency> dependencies = dependencyGraph.dependencies(snapshot.snapshotId());
            for (KnowledgeSnapshotDependency dep : dependencies) {
                if (dep.targetId() == null || dep.targetId().isBlank()) {
                    missing.add("dependency:" + dep.dependencyId());
                } else {
                    verified.add("dependency:" + dep.dependencyId());
                }
            }
        }

        KnowledgeSnapshotIntegrityStatus status;
        if (!mismatches.isEmpty()) {
            status = KnowledgeSnapshotIntegrityStatus.TAMPERED;
        } else if (policy.failOnMissingDependency() && !missing.isEmpty()) {
            status = KnowledgeSnapshotIntegrityStatus.INCOMPLETE;
        } else {
            status = KnowledgeSnapshotIntegrityStatus.ATTESTED;
        }

        return new KnowledgeSnapshotIntegrityResult(
                UUID.randomUUID().toString(),
                snapshot.snapshotId(),
                status,
                Instant.now(),
                mismatches,
                verified,
                missing,
                snapshot.aggregateFingerprint(),
                snapshot.aggregateFingerprint(),
                java.util.Map.of()
        );
    }
}
