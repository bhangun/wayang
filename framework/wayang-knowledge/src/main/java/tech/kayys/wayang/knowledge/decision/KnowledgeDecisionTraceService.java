package tech.kayys.wayang.knowledge.decision;

import java.util.List;
import java.util.Optional;

public interface KnowledgeDecisionTraceService {

    void record(KnowledgeDecisionTrace trace);

    Optional<KnowledgeDecisionTrace> get(String traceId);

    List<KnowledgeDecisionTrace> findByExecution(String executionId);

    List<KnowledgeDecisionTrace> findByAgent(String agentId);

    List<KnowledgeDecisionTrace> findByEvidence(String evidenceId);
}
