package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.Map;

/**
 * Result of a knowledge mutation.
 */
public record KnowledgeMutationResult(
        boolean success,
        String mutationId,
        KnowledgeItem resultingItem,
        String supersededItemId,
        String reason,
        Instant timestamp,
        Map<String, Object> metadata
) {

    public KnowledgeMutationResult {
        reason = reason == null ? "" : reason;
        timestamp = timestamp == null ? Instant.now() : timestamp;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeMutationResult success(String mutationId, KnowledgeItem resultingItem, String reason) {
        return new KnowledgeMutationResult(true, mutationId, resultingItem, null, reason, Instant.now(), Map.of());
    }

    public static KnowledgeMutationResult failure(String mutationId, String reason) {
        return new KnowledgeMutationResult(false, mutationId, null, null, reason, Instant.now(), Map.of());
    }
}
