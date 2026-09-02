package tech.kayys.wayang.knowledge.snapshot;

import java.util.List;
import java.util.Optional;

public interface KnowledgeDecisionSnapshotStore {

    void save(KnowledgeDecisionSnapshot snapshot);

    Optional<KnowledgeDecisionSnapshot> get(String snapshotId);

    Optional<KnowledgeDecisionSnapshot> getByTrace(String traceId);

    List<KnowledgeDecisionSnapshot> findByExecution(String executionId);
}
