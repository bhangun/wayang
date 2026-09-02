package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAttestationStore implements KnowledgeAttestationStore {

    private final Map<String, KnowledgeAttestation> attestations = new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeAttestation attestation) {
        attestations.put(attestation.attestationId(), attestation);
    }

    @Override
    public Optional<KnowledgeAttestation> get(String attestationId) {
        return Optional.ofNullable(attestations.get(attestationId));
    }

    @Override
    public List<KnowledgeAttestation> findBySnapshot(KnowledgeSnapshotId snapshotId) {
        return attestations.values().stream()
                .filter(a -> a.snapshotId().equals(snapshotId))
                .toList();
    }
}
