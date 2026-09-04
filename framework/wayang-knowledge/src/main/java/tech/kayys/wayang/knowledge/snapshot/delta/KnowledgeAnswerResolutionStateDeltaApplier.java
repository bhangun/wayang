package tech.kayys.wayang.knowledge.snapshot.delta;

import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;

public interface KnowledgeAnswerResolutionStateDeltaApplier {

    KnowledgeAnswerResolutionState apply(
            KnowledgeAnswerResolutionState source,
            KnowledgeAnswerResolutionStateDelta delta
    );
}
