package tech.kayys.wayang.knowledge.decision;

/**
 * Optional interface for execution/pipeline results exposing a decision trace.
 */
public interface KnowledgeDecisionTraceAware {

    KnowledgeDecisionTrace decisionTrace();

    default boolean hasDecisionTrace() {
        return decisionTrace() != null;
    }
}
