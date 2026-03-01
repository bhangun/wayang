package tech.kayys.wayang.rag;

public record RagScoredChunk(
        RagChunk chunk,
        double score) {
}
