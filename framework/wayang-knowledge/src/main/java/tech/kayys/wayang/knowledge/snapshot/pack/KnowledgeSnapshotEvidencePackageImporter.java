package tech.kayys.wayang.knowledge.snapshot.pack;

import java.io.InputStream;

public interface KnowledgeSnapshotEvidencePackageImporter {
    KnowledgeSnapshotEvidencePackage importPackage(InputStream input);
}
