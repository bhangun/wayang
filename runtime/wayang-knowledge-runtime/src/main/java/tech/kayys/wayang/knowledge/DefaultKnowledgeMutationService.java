package tech.kayys.wayang.knowledge;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Default implementation of KnowledgeMutationService connected to a VersionedKnowledgeStore.
 */
public class DefaultKnowledgeMutationService implements KnowledgeMutationService {

    private final VersionedKnowledgeStore store;

    public DefaultKnowledgeMutationService(VersionedKnowledgeStore store) {
        this.store = store != null ? store : new InMemoryVersionedKnowledgeStore();
    }

    @Override
    public CompletionStage<KnowledgeMutationResult> apply(KnowledgeMutationRequest request) {
        if (request == null) {
            return CompletableFuture.completedFuture(KnowledgeMutationResult.failure("unknown", "Request is null"));
        }

        return switch (request.type()) {
            case ADD -> store.save(request.draftItem())
                    .thenApply(saved -> KnowledgeMutationResult.success(request.id(), saved, "Knowledge added successfully"));

            case UPDATE -> store.get(request.targetItemId())
                    .thenCompose(opt -> {
                        if (opt.isEmpty()) {
                            return CompletableFuture.completedFuture(KnowledgeMutationResult.failure(request.id(), "Target item not found: " + request.targetItemId()));
                        }
                        return store.save(request.draftItem())
                                .thenApply(updated -> KnowledgeMutationResult.success(request.id(), updated, "Knowledge updated"));
                    });

            case SUPERSEDE -> store.get(request.supersedesId())
                    .thenCompose(optOld -> {
                        if (optOld.isPresent()) {
                            KnowledgeItem oldItem = optOld.get();
                            KnowledgeItem invalidatedOld = new KnowledgeItem(
                                    oldItem.id(), oldItem.sourceId(), oldItem.type(), oldItem.title(), oldItem.content(),
                                    oldItem.metadata(), oldItem.provenance(), oldItem.authority(),
                                    KnowledgeValidity.superseded(), oldItem.classification(), oldItem.sensitivity(),
                                    oldItem.trustLevel(), oldItem.revision()
                            );
                            store.save(invalidatedOld);
                        }
                        return store.save(request.draftItem())
                                .thenApply(savedNew -> new KnowledgeMutationResult(
                                        true, request.id(), savedNew, request.supersedesId(), "Superseded successfully",
                                        java.time.Instant.now(), java.util.Map.of()
                                ));
                    });

            case REVOKE -> store.get(request.targetItemId())
                    .thenCompose(opt -> {
                        if (opt.isEmpty()) {
                            return CompletableFuture.completedFuture(KnowledgeMutationResult.failure(request.id(), "Item not found"));
                        }
                        KnowledgeItem item = opt.get();
                        KnowledgeItem revoked = new KnowledgeItem(
                                item.id(), item.sourceId(), item.type(), item.title(), item.content(), item.metadata(),
                                item.provenance(), item.authority(), KnowledgeValidity.superseded(),
                                item.classification(), item.sensitivity(), item.trustLevel(), item.revision()
                        );
                        return store.save(revoked)
                                .thenApply(saved -> KnowledgeMutationResult.success(request.id(), saved, "Knowledge revoked"));
                    });

            default -> CompletableFuture.completedFuture(KnowledgeMutationResult.failure(request.id(), "Unsupported mutation: " + request.type()));
        };
    }
}
