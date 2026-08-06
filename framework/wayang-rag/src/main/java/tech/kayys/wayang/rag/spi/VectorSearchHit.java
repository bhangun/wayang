package tech.kayys.wayang.rag.spi;

import tech.kayys.wayang.rag.model.RagChunk;

/** A single result returned by a vector search. Part of the wayang-rag SPI. */
public record VectorSearchHit(RagChunk payload, double score, String id) {}
