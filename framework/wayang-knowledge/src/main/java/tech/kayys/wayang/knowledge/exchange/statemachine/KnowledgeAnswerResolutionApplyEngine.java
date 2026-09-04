package tech.kayys.wayang.knowledge.exchange.statemachine;

import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntry;

public interface KnowledgeAnswerResolutionApplyEngine {

    void apply(KnowledgeAnswerResolutionLogEntry entry);

    long lastAppliedIndex();

    String stateFingerprint();

    KnowledgeAnswerResolutionState state();
}
