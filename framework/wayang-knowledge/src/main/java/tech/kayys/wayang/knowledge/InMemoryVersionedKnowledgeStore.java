package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe versioned knowledge store.
 */
public class InMemoryVersionedKnowledgeStore implements VersionedKnowledgeStore {

    private final Map<String, List<KnowledgeItem>> store = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<KnowledgeItem> save(KnowledgeItem item) {
        if (item == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Item must not be null"));
        }

        store.compute(item.id(), (id, history) -> {
            List<KnowledgeItem> list = history != null ? new ArrayList<>(history) : new ArrayList<>();
            long nextRev = list.isEmpty() ? 1L : list.get(list.size() - 1).revision() + 1;
            KnowledgeItem revItem = item.withRevision(nextRev);
            list.add(revItem);
            return Collections.synchronizedList(list);
        });

        List<KnowledgeItem> updated = store.get(item.id());
        return CompletableFuture.completedFuture(updated.get(updated.size() - 1));
    }

    @Override
    public CompletionStage<Optional<KnowledgeItem>> get(String id) {
        List<KnowledgeItem> list = store.get(id);
        if (list == null || list.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.completedFuture(Optional.of(list.get(list.size() - 1)));
    }

    @Override
    public CompletionStage<Optional<KnowledgeItem>> getAsOf(String id, Instant asOf) {
        List<KnowledgeItem> list = store.get(id);
        if (list == null || list.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        Instant target = asOf != null ? asOf : Instant.now();
        for (int i = list.size() - 1; i >= 0; i--) {
            KnowledgeItem item = list.get(i);
            if (item.isCurrentlyValid(target)) {
                return CompletableFuture.completedFuture(Optional.of(item));
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletionStage<List<KnowledgeItem>> listHistory(String id) {
        List<KnowledgeItem> list = store.getOrDefault(id, List.of());
        return CompletableFuture.completedFuture(List.copyOf(list));
    }

    @Override
    public CompletionStage<List<KnowledgeItem>> query(KnowledgeQuery query, KnowledgeContext context) {
        Instant asOf = query.asOf() != null ? query.asOf() : (context != null ? context.asOf() : Instant.now());
        String term = query.text().toLowerCase();

        List<KnowledgeItem> matches = new ArrayList<>();
        for (List<KnowledgeItem> history : store.values()) {
            if (history == null || history.isEmpty()) continue;
            KnowledgeItem latest = history.get(history.size() - 1);
            if (latest.isCurrentlyValid(asOf)) {
                if (term.isEmpty() || latest.title().toLowerCase().contains(term) || latest.content().toLowerCase().contains(term)) {
                    matches.add(latest);
                }
            }
        }

        List<KnowledgeItem> limited = matches.stream().limit(query.topK()).toList();
        return CompletableFuture.completedFuture(limited);
    }

    @Override
    public CompletionStage<Boolean> delete(String id) {
        List<KnowledgeItem> removed = store.remove(id);
        return CompletableFuture.completedFuture(removed != null);
    }
}
