package tech.kayys.wayang.knowledge.snapshot.artifact;

import tech.kayys.wayang.knowledge.snapshot.pack.KnowledgeSnapshotEvidencePackage;

public interface KnowledgeSnapshotEvidencePackageSerializer {

    byte[] serialize(KnowledgeSnapshotEvidencePackage evidencePackage);

    KnowledgeSnapshotEvidencePackage deserialize(byte[] content);
}
