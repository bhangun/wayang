package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Storage contract supporting revisioned and point-in-time temporal knowledge retrieval.
 */
public interface VersionedKnowledgeStore {

    CompletionStage<KnowledgeItem> save(KnowledgeItem item);

    CompletionStage<Optional<KnowledgeItem>> get(String id);

    CompletionStage<Optional<KnowledgeItem>> getAsOf(String id, Instant asOf);

    CompletionStage<List<KnowledgeItem>> listHistory(String id);

    CompletionStage<List<KnowledgeItem>> query(KnowledgeQuery query, KnowledgeContext context);

    CompletionStage<Boolean> delete(String id);
}
