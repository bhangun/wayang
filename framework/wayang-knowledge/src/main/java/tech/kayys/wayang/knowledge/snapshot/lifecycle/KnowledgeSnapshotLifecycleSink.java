package tech.kayys.wayang.knowledge.snapshot.lifecycle;

public interface KnowledgeSnapshotLifecycleSink {

    void publish(KnowledgeSnapshotLifecycleEvent event);
}
