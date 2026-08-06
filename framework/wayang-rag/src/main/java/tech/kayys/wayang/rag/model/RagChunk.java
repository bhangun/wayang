package tech.kayys.wayang.rag.model;

import java.util.Map;
import java.util.UUID;

/** A text chunk produced by splitting a RagDocument. Part of the wayang-rag SPI. */
public record RagChunk(
        String id,
        String documentId,
        int chunkIndex,
        String text,
        Map<String, Object> metadata) {

    public RagChunk {
        metadata = RagMetadata.copy(metadata);
    }

    public static RagChunk of(String documentId, int chunkIndex, String text, Map<String, Object> metadata) {
        return new RagChunk(UUID.randomUUID().toString(), documentId, chunkIndex, text, metadata);
    }
}
