package tech.kayys.wayang.knowledge.decision;

/**
 * High-level outcome of a governed knowledge decision.
 */
public enum KnowledgeDecisionStatus {
    ALLOWED,
    DENIED,
    APPROVAL_REQUIRED,
    AMBIGUOUS,
    COMPLETED,
    FAILED
}
