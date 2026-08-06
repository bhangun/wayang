package tech.kayys.wayang.rag.model;

import java.util.Map;

/** Represents a user query to the RAG pipeline. Part of the wayang-rag SPI. */
public record RagQuery(
        String text,
        int topK,
        double minScore,
        Map<String, Object> filters) {

    public RagQuery {
        filters = RagMetadata.copy(filters);
    }

    public static RagQuery of(String text) {
        return new RagQuery(text, 5, 0.0, Map.of());
    }
}
