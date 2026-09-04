package tech.kayys.wayang.knowledge.exchange.statemachine;

import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntry;

public interface KnowledgeAnswerResolutionStateMachine {

    void apply(KnowledgeAnswerResolutionLogEntry entry);

    KnowledgeAnswerResolutionState state();

    String stateFingerprint();

    void reset();
}
