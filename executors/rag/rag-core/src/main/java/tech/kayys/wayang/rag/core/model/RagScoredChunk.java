package tech.kayys.wayang.rag.core.model;

public record RagScoredChunk(
        RagChunk chunk,
        double score) {
}
