package tech.kayys.wayang.knowledge.audit;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.Objects;

public final class DefaultKnowledgeAuditPolicy implements KnowledgeAuditPolicy {

    @Override
    public boolean shouldAudit(KnowledgeDecisionTrace trace) {
        return Objects.nonNull(trace);
    }
}
