package tech.kayys.wayang.knowledge.audit;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.List;

public interface KnowledgeAuditService {

    void audit(KnowledgeAuditEvent event);

    void audit(KnowledgeDecisionTrace trace, KnowledgeAuditContext context);

    List<KnowledgeAuditEvent> query(KnowledgeAuditQuery query);
}
