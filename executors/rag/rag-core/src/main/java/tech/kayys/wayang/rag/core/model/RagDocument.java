package tech.kayys.wayang.rag.core.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RagDocument(
        String id,
        String source,
        String content,
        Map<String, Object> metadata,
        Instant createdAt) {

    public static RagDocument of(String source, String content, Map<String, Object> metadata) {
        return new RagDocument(
                UUID.randomUUID().toString(),
                source,
                content,
                metadata == null ? Map.of() : Map.copyOf(metadata),
                Instant.now());
    }
}
