package tech.kayys.wayang.rag.model;

import java.util.Map;
import java.util.UUID;

/**
 * A document ingested into the RAG system.
 * Part of the wayang-rag SPI.
 */
public record RagDocument(
        String id,
        String content,
        Map<String, Object> metadata) {

    public RagDocument {
        metadata = RagMetadata.copy(metadata);
    }

    public static RagDocument of(String content, Map<String, Object> metadata) {
        return of(null, content, metadata);
    }

    public static RagDocument of(String id, String content, Map<String, Object> metadata) {
        String documentId = id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id.trim();
        return new RagDocument(documentId, content, metadata);
    }
}
