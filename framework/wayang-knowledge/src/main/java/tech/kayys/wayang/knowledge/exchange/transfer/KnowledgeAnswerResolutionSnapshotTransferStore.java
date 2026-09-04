package tech.kayys.wayang.knowledge.exchange.transfer;

import java.util.Optional;

public interface KnowledgeAnswerResolutionSnapshotTransferStore {

    void save(KnowledgeAnswerResolutionSnapshotTransferState state);

    Optional<KnowledgeAnswerResolutionSnapshotTransferState> get(String transferId);

    void delete(String transferId);
}
