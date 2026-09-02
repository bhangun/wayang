package tech.kayys.wayang.knowledge.snapshot.pack;

import java.io.OutputStream;

public interface KnowledgeSnapshotEvidencePackageExporter {
    void export(
            KnowledgeSnapshotEvidencePackage evidencePackage,
            OutputStream output
    );
}
