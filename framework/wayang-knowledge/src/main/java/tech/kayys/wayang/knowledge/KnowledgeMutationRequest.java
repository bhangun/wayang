package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Request to mutate knowledge in the system.
 */
public record KnowledgeMutationRequest(
        String id,
        KnowledgeMutationType type,
        String targetItemId,
        KnowledgeItem draftItem,
        String reason,
        String author,
        Instant effectiveFrom,
        String supersedesId,
        Map<String, Object> metadata
) {

    public KnowledgeMutationRequest {
        Objects.requireNonNull(type, "type must not be null");
        id = id == null ? "mut-" + java.util.UUID.randomUUID() : id;
        reason = reason == null ? "" : reason;
        author = author == null ? "system" : author;
        effectiveFrom = effectiveFrom == null ? Instant.now() : effectiveFrom;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeMutationRequest add(KnowledgeItem item, String author, String reason) {
        return new KnowledgeMutationRequest(null, KnowledgeMutationType.ADD, item.id(), item, reason, author, Instant.now(), null, Map.of());
    }

    public static KnowledgeMutationRequest supersede(String oldId, KnowledgeItem newItem, String author, String reason) {
        return new KnowledgeMutationRequest(null, KnowledgeMutationType.SUPERSEDE, newItem.id(), newItem, reason, author, Instant.now(), oldId, Map.of());
    }

    public static KnowledgeMutationRequest revoke(String itemId, String author, String reason) {
        return new KnowledgeMutationRequest(null, KnowledgeMutationType.REVOKE, itemId, null, reason, author, Instant.now(), null, Map.of());
    }
}
