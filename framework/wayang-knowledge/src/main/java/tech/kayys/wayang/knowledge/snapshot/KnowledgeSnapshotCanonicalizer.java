package tech.kayys.wayang.knowledge.snapshot;

public interface KnowledgeSnapshotCanonicalizer {

    String canonicalize(KnowledgeDecisionSnapshot snapshot);
}
