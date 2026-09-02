package tech.kayys.wayang.knowledge.decision;

import java.util.List;
import java.util.Optional;

/**
 * Persistence SPI for decision traces.
 */
public interface KnowledgeDecisionTraceStore {

    void save(KnowledgeDecisionTrace trace);

    Optional<KnowledgeDecisionTrace> get(String traceId);

    List<KnowledgeDecisionTrace> findByExecution(String executionId);

    List<KnowledgeDecisionTrace> findByAgent(String agentId);

    List<KnowledgeDecisionTrace> findByEvidence(String evidenceId);
}
