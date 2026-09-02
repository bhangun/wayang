package tech.kayys.wayang.knowledge.reasoning;

import tech.kayys.wayang.knowledge.KnowledgeContext;

import java.util.concurrent.CompletionStage;

/**
 * Generic decision and reasoning extension point.
 */
public interface DecisionEngine {

    CompletionStage<Decision> decide(
            DecisionRequest request,
            KnowledgeContext context
    );
}
