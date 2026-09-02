package tech.kayys.wayang.knowledge.audit;

import java.util.List;

public interface KnowledgeAuditStore extends KnowledgeAuditSink {

    List<KnowledgeAuditEvent> query(KnowledgeAuditQuery query);
}
