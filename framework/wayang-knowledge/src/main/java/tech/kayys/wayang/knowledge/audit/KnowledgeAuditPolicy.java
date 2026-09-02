package tech.kayys.wayang.knowledge.audit;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

public interface KnowledgeAuditPolicy {

    boolean shouldAudit(KnowledgeDecisionTrace trace);
}
