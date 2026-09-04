package tech.kayys.wayang.knowledge.exchange.transfer;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAnswerResolutionSnapshotTransferStore
        implements KnowledgeAnswerResolutionSnapshotTransferStore {

    private final ConcurrentHashMap<String, KnowledgeAnswerResolutionSnapshotTransferState> states =
            new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeAnswerResolutionSnapshotTransferState state) {
        states.put(state.transferId(), state);
    }

    @Override
    public Optional<KnowledgeAnswerResolutionSnapshotTransferState> get(String transferId) {
        return Optional.ofNullable(states.get(transferId));
    }

    @Override
    public void delete(String transferId) {
        states.remove(transferId);
    }
}
