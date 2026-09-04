package tech.kayys.wayang.knowledge.snapshot.delta;

import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;

public interface KnowledgeAnswerResolutionStateDeltaBuilder {

    KnowledgeAnswerResolutionStateDelta build(
            KnowledgeAnswerResolutionState source,
            KnowledgeAnswerResolutionState target,
            String sourceSnapshotId,
            String targetSnapshotId,
            String tenantId,
            String sourceEpochId,
            String targetEpochId
    );
}
