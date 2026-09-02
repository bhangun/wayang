package tech.kayys.wayang.knowledge.snapshot;

public interface KnowledgeSnapshotValidator {

    KnowledgeSnapshotValidation validate(KnowledgeDecisionSnapshot snapshot);
}
