package tech.kayys.wayang.knowledge.context;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.context.ContextType;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.resource.ResourceType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Bridges the generic KnowledgeResolver into the runtime ContextProvider pipeline.
 */
@ApplicationScoped
public class KnowledgeContextProvider implements ContextProvider {

    private final KnowledgeResolver resolver;
    private final KnowledgeBudget budget;

    @Inject
    public KnowledgeContextProvider(KnowledgeResolver resolver) {
        this.resolver = resolver;
        this.budget = KnowledgeBudget.defaults();
    }

    public KnowledgeContextProvider(KnowledgeResolver resolver, KnowledgeBudget budget) {
        this.resolver = resolver;
        this.budget = budget != null ? budget : KnowledgeBudget.defaults();
    }

    @Override
    public ResourceId id() {
        return new ResourceId.CustomId(Id.random(), new ResourceType.Custom("knowledge"));
    }

    @Override
    public ResourceType type() {
        return new ResourceType.Custom("knowledge");
    }

    @Override
    public Metadata metadata() {
        return Metadata.empty();
    }

    @Override
    public ContextData load(AgentContext context) throws Exception {
        return loadWithQuery(context, resolveQuery(context));
    }

    @Override
    public ContextData loadWithQuery(AgentContext context, String query) throws Exception {
        if (query == null || query.isBlank()) {
            return ContextData.empty();
        }

        KnowledgeQuery kQuery = KnowledgeQuery.of(query).withTopK(budget.maxItems()).withMinScore(budget.minimumScore());
        KnowledgeContext kCtx = KnowledgeContext.empty();

        KnowledgeResult result = resolver.resolve(kQuery, kCtx).toCompletableFuture().join();
        if (result == null || result.evidence().isEmpty()) {
            return ContextData.empty();
        }

        return toContextData(result);
    }

    @Override
    public CompletionStage<ContextData> loadStage(AgentContext context) {
        String query = resolveQuery(context);
        if (query == null || query.isBlank()) {
            return CompletableFuture.completedFuture(ContextData.empty());
        }

        KnowledgeQuery kQuery = KnowledgeQuery.of(query).withTopK(budget.maxItems()).withMinScore(budget.minimumScore());
        KnowledgeContext kCtx = KnowledgeContext.empty();

        return resolver.resolve(kQuery, kCtx)
                .thenApply(this::toContextData);
    }

    @Override
    public Set<ContextType> getSupportedTypes() {
        return Set.of(ContextType.KNOWLEDGE, ContextType.EVIDENCE);
    }

    @Override
    public boolean isAvailable(AgentContext context) {
        return resolver != null;
    }

    private String resolveQuery(AgentContext context) {
        if (context == null || context.request() == null) {
            return "";
        }
        return context.request().content();
    }

    private ContextData toContextData(KnowledgeResult result) {
        if (result == null || result.evidence().isEmpty()) {
            return ContextData.empty();
        }

        List<Object> knowledgeObjects = result.evidence().stream()
                .map(ev -> (Object) ev)
                .toList();

        return new ContextData(
                "k-" + java.util.UUID.randomUUID(),
                result.query() != null ? result.query().text() : "",
                "",
                List.of(),
                List.of(),
                knowledgeObjects,
                Map.of(),
                result.metadata(),
                1.0,
                0L,
                "knowledge"
        );
    }
}
