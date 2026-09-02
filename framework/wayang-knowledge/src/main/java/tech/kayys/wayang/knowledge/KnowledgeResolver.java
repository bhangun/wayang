package tech.kayys.wayang.knowledge;

import java.util.concurrent.CompletionStage;

/**
 * Resolves knowledge queries across registered knowledge sources.
 */
public interface KnowledgeResolver {

    CompletionStage<KnowledgeResult> resolve(
            KnowledgeQuery query,
            KnowledgeContext context
    );
}
