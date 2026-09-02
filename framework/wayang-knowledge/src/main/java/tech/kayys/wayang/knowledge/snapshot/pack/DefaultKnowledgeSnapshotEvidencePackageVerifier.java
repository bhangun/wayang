package tech.kayys.wayang.knowledge.snapshot.pack;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityService;
import tech.kayys.wayang.knowledge.seal.KnowledgeSnapshotSealVerificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultKnowledgeSnapshotEvidencePackageVerifier
        implements KnowledgeSnapshotEvidencePackageVerifier {

    private final KnowledgeSnapshotIntegrityService integrityService;
    private final KnowledgeSnapshotSealVerificationService sealService;

    public DefaultKnowledgeSnapshotEvidencePackageVerifier(
            KnowledgeSnapshotIntegrityService integrityService,
            KnowledgeSnapshotSealVerificationService sealService
    ) {
        this.integrityService = integrityService;
        this.sealService = sealService;
    }

    @Override
    public KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage
    ) {
        return verify(evidencePackage, KnowledgeSnapshotPackageVerificationContext.strict());
    }

    @Override
    public KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage,
            KnowledgeSnapshotPackageVerificationContext context
    ) {
        List<KnowledgeSnapshotPackageVerificationIssue> issues = new ArrayList<>();
        KnowledgeSnapshotVerificationManifest manifest = evidencePackage.manifest();

        if (manifest == null) {
            issues.add(new KnowledgeSnapshotPackageVerificationIssue(
                    "MANIFEST_MISSING",
                    "Verification manifest is missing",
                    null,
                    true,
                    Map.of()
            ));
            return result(evidencePackage, KnowledgeSnapshotPackageVerificationStatus.INVALID_MANIFEST, issues, false, false, false);
        }

        if (manifest.snapshotId() == null || manifest.snapshotFingerprint() == null) {
            issues.add(new KnowledgeSnapshotPackageVerificationIssue(
                    "SNAPSHOT_IDENTITY_MISSING",
                    "Snapshot identity is incomplete",
                    null,
                    true,
                    Map.of()
            ));
            return result(evidencePackage, KnowledgeSnapshotPackageVerificationStatus.INVALID_MANIFEST, issues, false, false, false);
        }

        // Verify resource fingerprints
        for (KnowledgeSnapshotPackageResource resource : evidencePackage.resources()) {
            String actual = KnowledgeSnapshotPackageFingerprint.sha256(resource.content());
            if (!actual.equalsIgnoreCase(resource.fingerprint()) && !resource.fingerprint().equalsIgnoreCase("sha256:" + actual)) {
                issues.add(new KnowledgeSnapshotPackageVerificationIssue(
                        "RESOURCE_FINGERPRINT_MISMATCH",
                        "Package resource fingerprint mismatch",
                        resource.resourceId(),
                        true,
                        Map.of("expected", resource.fingerprint(), "actual", actual)
                ));
            }
        }

        if (!issues.isEmpty()) {
            return result(evidencePackage, KnowledgeSnapshotPackageVerificationStatus.FINGERPRINT_MISMATCH, issues, false, false, false);
        }

        boolean integrityVerified = verifyIntegrity(manifest);
        if (context.requireIntegrity() && !integrityVerified) {
            issues.add(new KnowledgeSnapshotPackageVerificationIssue(
                    "INTEGRITY_FAILED",
                    "Snapshot integrity could not be verified",
                    manifest.snapshotId(),
                    true,
                    Map.of()
            ));
            return result(evidencePackage, KnowledgeSnapshotPackageVerificationStatus.INTEGRITY_FAILED, issues, false, false, false);
        }

        boolean sealVerified = true;
        if (manifest.seal() != null) {
            sealVerified = verifySeal(manifest);
            if (!sealVerified) {
                issues.add(new KnowledgeSnapshotPackageVerificationIssue(
                        "SEAL_FAILED",
                        "Secure seal verification failed",
                        manifest.snapshotId(),
                        true,
                        Map.of()
                ));
            }
        } else if (context.requireSeal()) {
            sealVerified = false;
            issues.add(new KnowledgeSnapshotPackageVerificationIssue(
                    "SEAL_MISSING",
                    "Secure seal is missing but required",
                    manifest.snapshotId(),
                    true,
                    Map.of()
            ));
        }

        if (!sealVerified) {
            return result(evidencePackage, KnowledgeSnapshotPackageVerificationStatus.SEAL_FAILED, issues, integrityVerified, false, false);
        }

        return result(evidencePackage, KnowledgeSnapshotPackageVerificationStatus.VERIFIED, issues, integrityVerified, sealVerified, true);
    }

    private boolean verifyIntegrity(KnowledgeSnapshotVerificationManifest manifest) {
        return manifest.integrity() != null &&
                ("ATTESTED".equalsIgnoreCase(manifest.integrity().status()) ||
                 "VERIFIED".equalsIgnoreCase(manifest.integrity().status()));
    }

    private boolean verifySeal(KnowledgeSnapshotVerificationManifest manifest) {
        return manifest.seal() != null &&
                ("SEALED".equalsIgnoreCase(manifest.seal().status()) ||
                 "VERIFIED".equalsIgnoreCase(manifest.seal().status()));
    }

    private KnowledgeSnapshotPackageVerificationResult result(
            KnowledgeSnapshotEvidencePackage evidencePackage,
            KnowledgeSnapshotPackageVerificationStatus status,
            List<KnowledgeSnapshotPackageVerificationIssue> issues,
            boolean integrity,
            boolean seal,
            boolean dependencies
    ) {
        KnowledgeSnapshotVerificationManifest manifest = evidencePackage.manifest();
        return new KnowledgeSnapshotPackageVerificationResult(
                evidencePackage.packageId(),
                status,
                manifest == null ? null : manifest.snapshotId(),
                manifest == null ? null : manifest.snapshotFingerprint(),
                issues,
                integrity,
                seal,
                dependencies,
                Map.of()
        );
    }
}
