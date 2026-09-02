package tech.kayys.wayang.knowledge.decision;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory decision trace store.
 */
public class InMemoryKnowledgeDecisionTraceStore implements KnowledgeDecisionTraceStore {

    private final Map<String, KnowledgeDecisionTrace> traces = new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeDecisionTrace trace) {
        if (trace == null || trace.id() == null) {
            throw new IllegalArgumentException("trace and trace.id are required");
        }
        traces.put(trace.id(), trace);
    }

    @Override
    public Optional<KnowledgeDecisionTrace> get(String traceId) {
        return Optional.ofNullable(traces.get(traceId));
    }

    @Override
    public List<KnowledgeDecisionTrace> findByExecution(String executionId) {
        return traces.values().stream()
                .filter(t -> executionId != null && executionId.equals(t.executionId()))
                .toList();
    }

    @Override
    public List<KnowledgeDecisionTrace> findByAgent(String agentId) {
        return traces.values().stream()
                .filter(t -> agentId != null && agentId.equals(t.agentId()))
                .toList();
    }

    @Override
    public List<KnowledgeDecisionTrace> findByEvidence(String evidenceId) {
        if (evidenceId == null) {
            return List.of();
        }
        return traces.values().stream()
                .filter(t -> t.evidenceIds().contains(evidenceId))
                .toList();
    }
}
