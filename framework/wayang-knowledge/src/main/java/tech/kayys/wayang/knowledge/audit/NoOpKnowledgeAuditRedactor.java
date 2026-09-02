package tech.kayys.wayang.knowledge.audit;

public final class NoOpKnowledgeAuditRedactor implements KnowledgeAuditRedactor {

    @Override
    public KnowledgeAuditEvent redact(KnowledgeAuditEvent event) {
        return event;
    }
}
