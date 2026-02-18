package tech.kayys.wayang.rag.core.model;

import java.util.List;
import java.util.Map;

public record RagResult(
        RagQuery query,
        List<RagScoredChunk> chunks,
        String answer,
        Map<String, Object> metadata) {
}
