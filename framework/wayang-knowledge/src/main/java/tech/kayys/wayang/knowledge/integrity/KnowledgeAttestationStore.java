package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;
import java.util.Optional;

public interface KnowledgeAttestationStore {

    void save(KnowledgeAttestation attestation);

    Optional<KnowledgeAttestation> get(String attestationId);

    List<KnowledgeAttestation> findBySnapshot(KnowledgeSnapshotId snapshotId);
}
