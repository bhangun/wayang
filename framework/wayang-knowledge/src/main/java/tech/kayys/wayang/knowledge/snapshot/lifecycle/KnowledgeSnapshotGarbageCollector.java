package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import java.time.Instant;
import java.util.List;

public interface KnowledgeSnapshotGarbageCollector {

    List<KnowledgeSnapshotLifecycleDecision> collect(Instant now);
}
