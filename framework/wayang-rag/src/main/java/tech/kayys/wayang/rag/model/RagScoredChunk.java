package tech.kayys.wayang.rag.model;

/** A retrieved chunk with its similarity score. Part of the wayang-rag SPI. */
public record RagScoredChunk(RagChunk chunk, double score) {}
