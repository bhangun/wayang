package tech.kayys.wayang.knowledge.snapshot.dependency;

import java.time.Instant;

public interface KnowledgeSnapshotReachabilityAnalyzer {

    KnowledgeSnapshotReachability analyze(Instant now);
}
