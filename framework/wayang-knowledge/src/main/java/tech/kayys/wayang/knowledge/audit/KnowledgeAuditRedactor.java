package tech.kayys.wayang.knowledge.audit;

public interface KnowledgeAuditRedactor {

    KnowledgeAuditEvent redact(KnowledgeAuditEvent event);
}
