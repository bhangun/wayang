package tech.kayys.wayang.knowledge;

import java.util.concurrent.CompletionStage;
import java.util.Map;

/**
 * Resolves execution-scoped knowledge for an agent request.
 */
public interface KnowledgeContextResolver {

    CompletionStage<KnowledgeResult> resolve(
            String requestQuery,
            KnowledgeContext knowledgeContext
    );

    default boolean isEnabled(KnowledgeContext knowledgeContext) {
        return true;
    }
}
