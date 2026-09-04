package tech.kayys.wayang.knowledge.exchange.transfer;

import tech.kayys.wayang.knowledge.exchange.checkpoint.KnowledgeAnswerResolutionStateCheckpoint;

public interface KnowledgeAnswerResolutionSnapshotSink {

    void begin(KnowledgeAnswerResolutionSnapshotDescriptor descriptor);

    void write(KnowledgeAnswerResolutionSnapshotTransferChunk chunk);

    KnowledgeAnswerResolutionStateCheckpoint complete();

    void abort();
}
