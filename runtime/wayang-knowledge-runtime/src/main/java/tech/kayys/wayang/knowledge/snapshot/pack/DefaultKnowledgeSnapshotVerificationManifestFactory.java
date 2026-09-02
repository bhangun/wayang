package tech.kayys.wayang.knowledge.snapshot.pack;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityResult;
import tech.kayys.wayang.knowledge.seal.KnowledgeSnapshotSecureSeal;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultKnowledgeSnapshotVerificationManifestFactory
        implements KnowledgeSnapshotVerificationManifestFactory {

    @Override
    public KnowledgeSnapshotVerificationManifest create(
            KnowledgeDecisionSnapshot snapshot,
            KnowledgeSnapshotIntegrityResult integrity,
            KnowledgeSnapshotSecureSeal seal
    ) {
        List<KnowledgeSnapshotEvidenceEntry> evidence = snapshot.knowledge()
                .stream()
                .map(entry -> {
                    Map<String, String> meta = new HashMap<>();
                    if (entry.metadata() != null) {
                        entry.metadata().forEach((k, v) -> meta.put(k, String.valueOf(v)));
                    }
                    return new KnowledgeSnapshotEvidenceEntry(
                            entry.knowledgeId(),
                            entry.versionId(),
                            entry.fingerprint(),
                            entry.provenanceId(),
                            entry.authorityFingerprint(),
                            entry.trustFingerprint(),
                            entry.lineageFingerprint(),
                            true,
                            false,
                            meta
                    );
                })
                .toList();

        KnowledgeSnapshotIntegrityManifest integrityManifest = integrity == null ? null :
                new KnowledgeSnapshotIntegrityManifest(
                        integrity.status().name(),
                        integrity.verificationId(),
                        "1.0.0",
                        integrity.verifiedAt(),
                        integrity.mismatches(),
                        integrity.computedFingerprint(),
                        integrity.metadata()
                );

        KnowledgeSnapshotSealManifest sealManifest = seal == null ? null :
                new KnowledgeSnapshotSealManifest(
                        seal.sealId(),
                        seal.algorithm().name(),
                        seal.anchorType().name(),
                        seal.keyId(),
                        seal.keyVersion(),
                        seal.signature(),
                        seal.createdAt(),
                        seal.expiresAt(),
                        seal.status().name(),
                        seal.metadata()
                );

        Instant govEffectiveAt = null;
        if (snapshot.governance() != null && snapshot.governance().effectiveAt() != null) {
            try {
                govEffectiveAt = Instant.parse(snapshot.governance().effectiveAt());
            } catch (Exception ignored) {
                govEffectiveAt = Instant.now();
            }
        }

        Map<String, String> govAttrs = new HashMap<>();
        if (snapshot.governance() != null && snapshot.governance().attributes() != null) {
            snapshot.governance().attributes().forEach((k, v) -> govAttrs.put(k, String.valueOf(v)));
        }

        KnowledgeSnapshotGovernanceManifest governanceManifest = snapshot.governance() == null ? null :
                new KnowledgeSnapshotGovernanceManifest(
                        snapshot.governance().tenantId(),
                        snapshot.governance().workspaceId(),
                        snapshot.governance().projectId(),
                        snapshot.governance().userId(),
                        govEffectiveAt,
                        snapshot.governance().scopeFingerprint(),
                        snapshot.governance().governancePolicyFingerprint(),
                        govAttrs
                );

        String runtimeVersion = snapshot.runtime() != null && snapshot.runtime().runtimeVersion() != null
                ? snapshot.runtime().runtimeVersion() : "1.0.0";
        String knowledgeEngineVersion = snapshot.runtime() != null && snapshot.runtime().knowledgeEngineVersion() != null
                ? snapshot.runtime().knowledgeEngineVersion() : "1.0.0";

        List<KnowledgeSnapshotReferenceEntry> policies = snapshot.policies() == null ? List.of() :
                snapshot.policies().policies().stream()
                        .map(r -> new KnowledgeSnapshotReferenceEntry(
                                r.id(),
                                r.versionId(),
                                "policy",
                                r.fingerprint(),
                                true,
                                true,
                                Map.of()
                        ))
                        .toList();

        List<KnowledgeSnapshotReferenceEntry> rules = snapshot.rules() == null ? List.of() :
                snapshot.rules().rules().stream()
                        .map(r -> new KnowledgeSnapshotReferenceEntry(
                                r.id(),
                                r.versionId(),
                                "rule",
                                r.fingerprint(),
                                true,
                                true,
                                Map.of()
                        ))
                        .toList();

        return new KnowledgeSnapshotVerificationManifest(
                snapshot.snapshotId().value(),
                snapshot.snapshotId().value(),
                snapshot.aggregateFingerprint(),
                "1",
                runtimeVersion,
                knowledgeEngineVersion,
                evidence,
                List.of(),
                policies,
                rules,
                governanceManifest,
                integrityManifest,
                sealManifest,
                List.of(),
                Instant.now(),
                Map.of()
        );
    }
}
