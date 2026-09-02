package tech.kayys.wayang.knowledge.decision;

import java.time.Instant;
import java.util.*;

/**
 * Mutable builder/recorder used during execution to capture decision facts.
 */
public final class KnowledgeDecisionRecorder {

    private final String traceId;
    private String executionId;
    private String agentId;
    private String operation;
    private String query;
    private final Set<String> evidenceIds = new LinkedHashSet<>();
    private final Set<String> lineageIds = new LinkedHashSet<>();
    private final Set<String> policyIds = new LinkedHashSet<>();
    private final Set<String> ruleIds = new LinkedHashSet<>();
    private final List<KnowledgeDecisionStep> steps = new ArrayList<>();
    private KnowledgeDecisionOutcome outcome;
    private final Map<String, Object> metadata = new HashMap<>();

    public KnowledgeDecisionRecorder() {
        this(UUID.randomUUID().toString());
    }

    public KnowledgeDecisionRecorder(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId is required");
        }
        this.traceId = traceId;
    }

    public KnowledgeDecisionRecorder executionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public KnowledgeDecisionRecorder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public KnowledgeDecisionRecorder operation(String operation) {
        this.operation = operation;
        return this;
    }

    public KnowledgeDecisionRecorder query(String query) {
        this.query = query;
        return this;
    }

    public KnowledgeDecisionRecorder evidence(String evidenceId) {
        if (evidenceId != null) {
            evidenceIds.add(evidenceId);
        }
        return this;
    }

    public KnowledgeDecisionRecorder evidence(Iterable<String> ids) {
        if (ids != null) {
            ids.forEach(this::evidence);
        }
        return this;
    }

    public KnowledgeDecisionRecorder lineage(String lineageId) {
        if (lineageId != null) {
            lineageIds.add(lineageId);
        }
        return this;
    }

    public KnowledgeDecisionRecorder policy(String policyId) {
        if (policyId != null) {
            policyIds.add(policyId);
        }
        return this;
    }

    public KnowledgeDecisionRecorder rule(String ruleId) {
        if (ruleId != null) {
            ruleIds.add(ruleId);
        }
        return this;
    }

    public KnowledgeDecisionRecorder step(KnowledgeDecisionStep step) {
        if (step != null) {
            steps.add(step);
            evidenceIds.addAll(step.evidenceIds());
            policyIds.addAll(step.policyIds());
            ruleIds.addAll(step.ruleIds());
        }
        return this;
    }

    public KnowledgeDecisionRecorder outcome(KnowledgeDecisionOutcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public KnowledgeDecisionRecorder metadata(String key, Object value) {
        if (key != null && value != null) {
            metadata.put(key, value);
        }
        return this;
    }

    public KnowledgeDecisionTrace build() {
        return new KnowledgeDecisionTrace(
                traceId,
                executionId,
                agentId,
                operation,
                query,
                List.copyOf(evidenceIds),
                List.copyOf(lineageIds),
                List.copyOf(policyIds),
                List.copyOf(ruleIds),
                List.copyOf(steps),
                outcome,
                Instant.now(),
                Map.copyOf(metadata)
        );
    }
}
