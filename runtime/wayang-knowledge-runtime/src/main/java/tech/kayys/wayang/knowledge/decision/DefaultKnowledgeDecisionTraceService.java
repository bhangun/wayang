package tech.kayys.wayang.knowledge.decision;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultKnowledgeDecisionTraceService implements KnowledgeDecisionTraceService {

    private final KnowledgeDecisionTraceStore store;

    public DefaultKnowledgeDecisionTraceService(KnowledgeDecisionTraceStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    @Override
    public void record(KnowledgeDecisionTrace trace) {
        Objects.requireNonNull(trace, "trace must not be null");
        store.save(trace);
    }

    @Override
    public Optional<KnowledgeDecisionTrace> get(String traceId) {
        return store.get(traceId);
    }

    @Override
    public List<KnowledgeDecisionTrace> findByExecution(String executionId) {
        return store.findByExecution(executionId);
    }

    @Override
    public List<KnowledgeDecisionTrace> findByAgent(String agentId) {
        return store.findByAgent(agentId);
    }

    @Override
    public List<KnowledgeDecisionTrace> findByEvidence(String evidenceId) {
        return store.findByEvidence(evidenceId);
    }
}
