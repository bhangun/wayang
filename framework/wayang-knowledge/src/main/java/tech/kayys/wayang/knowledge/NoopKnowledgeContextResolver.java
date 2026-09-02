package tech.kayys.wayang.knowledge;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Safe default used when no knowledge extension is installed.
 */
public final class NoopKnowledgeContextResolver implements KnowledgeContextResolver {

    @Override
    public CompletionStage<KnowledgeResult> resolve(
            String requestQuery,
            KnowledgeContext knowledgeContext
    ) {
        return CompletableFuture.completedFuture(
                KnowledgeResult.empty(KnowledgeQuery.of(requestQuery != null ? requestQuery : ""))
        );
    }
}
