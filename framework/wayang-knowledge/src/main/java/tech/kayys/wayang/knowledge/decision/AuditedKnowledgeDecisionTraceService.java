package tech.kayys.wayang.knowledge.decision;

import tech.kayys.wayang.knowledge.audit.KnowledgeAuditContext;
import tech.kayys.wayang.knowledge.audit.KnowledgeAuditService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AuditedKnowledgeDecisionTraceService implements KnowledgeDecisionTraceService {

    private final KnowledgeDecisionTraceService delegate;
    private final KnowledgeAuditService auditService;
    private final KnowledgeAuditContext auditContext;

    public AuditedKnowledgeDecisionTraceService(
            KnowledgeDecisionTraceService delegate,
            KnowledgeAuditService auditService,
            KnowledgeAuditContext auditContext) {

        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
        this.auditService = Objects.requireNonNull(auditService, "auditService is required");
        this.auditContext = Objects.requireNonNull(auditContext, "auditContext is required");
    }

    @Override
    public void record(KnowledgeDecisionTrace trace) {
        delegate.record(trace);
        auditService.audit(trace, auditContext);
    }

    @Override
    public Optional<KnowledgeDecisionTrace> get(String traceId) {
        return delegate.get(traceId);
    }

    @Override
    public List<KnowledgeDecisionTrace> findByExecution(String executionId) {
        return delegate.findByExecution(executionId);
    }

    @Override
    public List<KnowledgeDecisionTrace> findByAgent(String agentId) {
        return delegate.findByAgent(agentId);
    }

    @Override
    public List<KnowledgeDecisionTrace> findByEvidence(String evidenceId) {
        return delegate.findByEvidence(evidenceId);
    }
}
