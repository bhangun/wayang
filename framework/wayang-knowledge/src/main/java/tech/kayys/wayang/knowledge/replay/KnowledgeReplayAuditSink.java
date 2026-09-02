package tech.kayys.wayang.knowledge.replay;

public interface KnowledgeReplayAuditSink {

    void publish(KnowledgeReplayAuditEvent event);
}
