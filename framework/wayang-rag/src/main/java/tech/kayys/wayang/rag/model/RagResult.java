package tech.kayys.wayang.rag.model;

import java.util.List;
import java.util.Map;

/** The full result of a RAG query: retrieved chunks + generated answer. Part of the wayang-rag SPI. */
public record RagResult(
        RagQuery query,
        List<RagScoredChunk> chunks,
        String answer,
        Map<String, Object> metadata) {

    public RagResult {
        metadata = RagMetadata.copy(metadata);
    }
}
