package tech.kayys.wayang.knowledge.governance;

import tech.kayys.wayang.knowledge.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Decorator enforcing governance boundaries around a VersionedKnowledgeStore.
 */
public class GovernedKnowledgeStore implements VersionedKnowledgeStore {

    private final VersionedKnowledgeStore delegate;
    private final KnowledgeGovernanceResolver governance;

    public GovernedKnowledgeStore(VersionedKnowledgeStore delegate, KnowledgeGovernanceResolver governance) {
        this.delegate = delegate;
        this.governance = governance != null ? governance : new DefaultKnowledgeGovernanceResolver();
    }

    @Override
    public CompletionStage<KnowledgeItem> save(KnowledgeItem item) {
        return delegate.save(item);
    }

    @Override
    public CompletionStage<Optional<KnowledgeItem>> get(String id) {
        return delegate.get(id);
    }

    @Override
    public CompletionStage<Optional<KnowledgeItem>> getAsOf(String id, Instant asOf) {
        return delegate.getAsOf(id, asOf);
    }

    @Override
    public CompletionStage<List<KnowledgeItem>> listHistory(String id) {
        return delegate.listHistory(id);
    }

    @Override
    public CompletionStage<List<KnowledgeItem>> query(KnowledgeQuery query, KnowledgeContext context) {
        return delegate.query(query, context)
                .thenApply(items -> items.stream()
                        .filter(item -> governance.isPermitted(item, context))
                        .toList());
    }

    @Override
    public CompletionStage<Boolean> delete(String id) {
        return delegate.delete(id);
    }
}
