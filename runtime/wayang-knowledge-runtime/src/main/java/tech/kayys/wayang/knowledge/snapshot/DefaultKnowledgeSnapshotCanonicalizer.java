package tech.kayys.wayang.knowledge.snapshot;

import java.util.Comparator;
import java.util.stream.Collectors;

public final class DefaultKnowledgeSnapshotCanonicalizer implements KnowledgeSnapshotCanonicalizer {

    @Override
    public String canonicalize(KnowledgeDecisionSnapshot snapshot) {
        String knowledge = snapshot.knowledge().stream()
                .sorted(Comparator.comparing(KnowledgeSnapshotEntry::knowledgeId))
                .map(e -> e.knowledgeId() + ":" + e.versionId() + ":" + nullSafe(e.fingerprint()))
                .collect(Collectors.joining("|"));

        String policies = snapshot.policies().policies().stream()
                .sorted(Comparator.comparing(KnowledgeVersionReference::id))
                .map(e -> e.id() + ":" + e.versionId() + ":" + nullSafe(e.fingerprint()))
                .collect(Collectors.joining("|"));

        String rules = snapshot.rules().rules().stream()
                .sorted(Comparator.comparing(KnowledgeVersionReference::id))
                .map(e -> e.id() + ":" + e.versionId() + ":" + nullSafe(e.fingerprint()))
                .collect(Collectors.joining("|"));

        return String.join(
                "\n",
                nullSafe(snapshot.executionId()),
                nullSafe(snapshot.traceId()),
                nullSafe(snapshot.agentId()),
                nullSafe(snapshot.operation()),
                nullSafe(snapshot.query()),
                nullSafe(snapshot.effectiveAt()),
                knowledge,
                policies,
                rules,
                nullSafe(snapshot.governance().scopeFingerprint()),
                nullSafe(snapshot.governance().governancePolicyFingerprint()),
                nullSafe(snapshot.runtime().runtimeVersion()),
                nullSafe(snapshot.runtime().knowledgeEngineVersion()),
                nullSafe(snapshot.runtime().rankingVersion()),
                nullSafe(snapshot.runtime().compressionVersion()),
                nullSafe(snapshot.runtime().budgetPolicyVersion()),
                nullSafe(snapshot.runtime().modelProviderId()),
                nullSafe(snapshot.runtime().modelId()),
                nullSafe(snapshot.runtime().modelVersion())
        );
    }

    private static String nullSafe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
