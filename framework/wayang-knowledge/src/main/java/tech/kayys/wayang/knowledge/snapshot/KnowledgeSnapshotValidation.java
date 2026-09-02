package tech.kayys.wayang.knowledge.snapshot;

import java.util.List;

public record KnowledgeSnapshotValidation(
        boolean valid,
        List<String> missingVersions,
        List<String> fingerprintMismatches,
        List<String> governanceMismatches,
        List<String> policyMismatches,
        List<String> ruleMismatches
) {

    public KnowledgeSnapshotValidation {
        missingVersions = missingVersions == null ? List.of() : List.copyOf(missingVersions);
        fingerprintMismatches = fingerprintMismatches == null ? List.of() : List.copyOf(fingerprintMismatches);
        governanceMismatches = governanceMismatches == null ? List.of() : List.copyOf(governanceMismatches);
        policyMismatches = policyMismatches == null ? List.of() : List.copyOf(policyMismatches);
        ruleMismatches = ruleMismatches == null ? List.of() : List.copyOf(ruleMismatches);
    }
}
