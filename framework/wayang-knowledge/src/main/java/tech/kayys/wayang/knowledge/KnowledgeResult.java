package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of a knowledge resolution or query.
 */
public record KnowledgeResult(
        KnowledgeQuery query,
        List<KnowledgeEvidence> evidence,
        Map<String, Object> metadata,
        Instant timestamp
) {

    public KnowledgeResult {
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public static KnowledgeResult empty(KnowledgeQuery query) {
        return new KnowledgeResult(query, List.of(), Map.of(), Instant.now());
    }

    public static KnowledgeResult of(KnowledgeQuery query, List<KnowledgeEvidence> evidence) {
        return new KnowledgeResult(query, evidence, Map.of(), Instant.now());
    }
}
