package tech.kayys.wayang.knowledge.audit;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeAuditService implements KnowledgeAuditService {

    private final KnowledgeAuditStore store;
    private final KnowledgeAuditPolicy policy;
    private final KnowledgeAuditRedactor redactor;

    public DefaultKnowledgeAuditService(
            KnowledgeAuditStore store,
            KnowledgeAuditPolicy policy,
            KnowledgeAuditRedactor redactor) {

        this.store = Objects.requireNonNull(store, "store is required");
        this.policy = Objects.requireNonNull(policy, "policy is required");
        this.redactor = Objects.requireNonNull(redactor, "redactor is required");
    }

    @Override
    public void audit(KnowledgeAuditEvent event) {
        Objects.requireNonNull(event, "event is required");
        if (!policy.shouldAudit(event.decisionTrace())) {
            return;
        }
        store.publish(redactor.redact(event));
    }

    @Override
    public void audit(KnowledgeDecisionTrace trace, KnowledgeAuditContext context) {
        Objects.requireNonNull(trace, "trace is required");
        Objects.requireNonNull(context, "context is required");

        if (!policy.shouldAudit(trace)) {
            return;
        }

        KnowledgeAuditEvent event = new KnowledgeAuditEvent(
                UUID.randomUUID().toString(),
                trace.executionId(),
                trace.agentId(),
                context.actorId(),
                context.tenantId(),
                context.workspaceId(),
                context.projectId(),
                trace.operation(),
                trace,
                Instant.now(),
                context.metadata()
        );

        audit(event);
    }

    @Override
    public List<KnowledgeAuditEvent> query(KnowledgeAuditQuery query) {
        return store.query(query);
    }
}
