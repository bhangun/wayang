package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Comparator;
import java.util.stream.Collectors;

public final class DefaultKnowledgeSnapshotVerificationManifestCanonicalizer
        implements KnowledgeSnapshotVerificationManifestCanonicalizer {

    @Override
    public String canonicalize(KnowledgeSnapshotVerificationManifest manifest) {
        String evidence = manifest.evidence()
                .stream()
                .sorted(Comparator.comparing(KnowledgeSnapshotEvidenceEntry::knowledgeId))
                .map(e -> e.knowledgeId() + "|" +
                        e.versionId() + "|" +
                        e.fingerprint() + "|" +
                        e.provenanceId() + "|" +
                        e.authorityFingerprint() + "|" +
                        e.trustFingerprint() + "|" +
                        e.lineageFingerprint())
                .collect(Collectors.joining("\n"));

        String lineage = manifest.lineage()
                .stream()
                .sorted(Comparator.comparing(KnowledgeSnapshotLineageEntry::lineageId))
                .map(e -> e.lineageId() + "|" +
                        e.sourceKnowledgeId() + "|" +
                        e.targetKnowledgeId() + "|" +
                        e.relationType())
                .collect(Collectors.joining("\n"));

        String policies = manifest.policies()
                .stream()
                .sorted(Comparator.comparing(KnowledgeSnapshotReferenceEntry::id))
                .map(e -> e.id() + "|" +
                        e.versionId() + "|" +
                        e.kind() + "|" +
                        e.fingerprint())
                .collect(Collectors.joining("\n"));

        String rules = manifest.rules()
                .stream()
                .sorted(Comparator.comparing(KnowledgeSnapshotReferenceEntry::id))
                .map(e -> e.id() + "|" +
                        e.versionId() + "|" +
                        e.kind() + "|" +
                        e.fingerprint())
                .collect(Collectors.joining("\n"));

        String merkleStr = manifest.merkle() != null
                ? "\nmerkle-root=" + manifest.merkle().rootHash() + "\nmerkle-leaves=" + manifest.merkle().leafCount()
                : "";

        return String.join("\n",
                "schema=" + manifest.schemaVersion(),
                "snapshot=" + manifest.snapshotId(),
                "fingerprint=" + manifest.snapshotFingerprint(),
                "runtime=" + manifest.runtimeVersion(),
                "knowledge-engine=" + manifest.knowledgeEngineVersion(),
                "evidence=" + evidence,
                "lineage=" + lineage,
                "policies=" + policies,
                "rules=" + rules
        ) + merkleStr;
    }
}
