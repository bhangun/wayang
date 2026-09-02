package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Domain-neutral request for knowledge retrieval.
 */
public record KnowledgeQuery(
        String text,
        int topK,
        double minScore,
        String scope,
        Instant asOf,
        Map<String, Object> filters,
        Map<String, Object> metadata
) {

    public KnowledgeQuery {
        text = Objects.requireNonNullElse(text, "").trim();

        if (topK < 1) {
            topK = 5;
        }

        if (minScore < 0.0) {
            minScore = 0.0;
        }

        scope = scope == null || scope.isBlank() ? "default" : scope;
        filters = filters == null ? Map.of() : Map.copyOf(filters);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeQuery of(String text) {
        return new KnowledgeQuery(text, 5, 0.0, "default", null, Map.of(), Map.of());
    }

    public KnowledgeQuery withTopK(int value) {
        return new KnowledgeQuery(text, value, minScore, scope, asOf, filters, metadata);
    }

    public KnowledgeQuery withScope(String value) {
        return new KnowledgeQuery(text, topK, minScore, value, asOf, filters, metadata);
    }

    public KnowledgeQuery asOf(Instant value) {
        return new KnowledgeQuery(text, topK, minScore, scope, value, filters, metadata);
    }

    public KnowledgeQuery withMinScore(double score) {
        return new KnowledgeQuery(text, topK, score, scope, asOf, filters, metadata);
    }
}
