package tech.kayys.wayang.knowledge.exchange.transfer;

public interface KnowledgeAnswerResolutionSnapshotSource {

    KnowledgeAnswerResolutionSnapshotDescriptor descriptor(String snapshotId);

    KnowledgeAnswerResolutionSnapshotTransferChunk read(
            KnowledgeAnswerResolutionSnapshotTransferRequest request);
}
