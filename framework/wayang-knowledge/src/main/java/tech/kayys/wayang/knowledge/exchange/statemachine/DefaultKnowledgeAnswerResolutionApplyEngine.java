package tech.kayys.wayang.knowledge.exchange.statemachine;

import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionCommitService;
import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionLogEntry;

import java.util.Objects;

public final class DefaultKnowledgeAnswerResolutionApplyEngine
        implements KnowledgeAnswerResolutionApplyEngine {

    private final KnowledgeAnswerResolutionStateMachine stateMachine;
    private final KnowledgeAnswerResolutionCommitService commitService;

    public DefaultKnowledgeAnswerResolutionApplyEngine(
            KnowledgeAnswerResolutionStateMachine stateMachine,
            KnowledgeAnswerResolutionCommitService commitService) {
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine");
        this.commitService = Objects.requireNonNull(commitService, "commitService");
    }

    @Override
    public synchronized void apply(KnowledgeAnswerResolutionLogEntry entry) {
        stateMachine.apply(entry);
        commitService.applyThrough(entry.index());
    }

    @Override
    public long lastAppliedIndex() {
        return stateMachine.state().lastAppliedIndex();
    }

    @Override
    public String stateFingerprint() {
        return stateMachine.stateFingerprint();
    }

    @Override
    public KnowledgeAnswerResolutionState state() {
        return stateMachine.state();
    }
}
