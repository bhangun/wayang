package tech.kayys.wayang.knowledge.snapshot.dependency;

import java.time.Instant;
import java.util.List;

public interface KnowledgeSnapshotCascadingGarbageCollector {

    List<KnowledgeSnapshotCascadingRetentionDecision> collect(Instant now);
}
