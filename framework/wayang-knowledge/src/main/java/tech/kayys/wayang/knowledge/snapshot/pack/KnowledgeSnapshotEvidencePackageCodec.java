package tech.kayys.wayang.knowledge.snapshot.pack;

public interface KnowledgeSnapshotEvidencePackageCodec {
    byte[] encode(KnowledgeSnapshotEvidencePackage evidencePackage);
    KnowledgeSnapshotEvidencePackage decode(byte[] data);
}
