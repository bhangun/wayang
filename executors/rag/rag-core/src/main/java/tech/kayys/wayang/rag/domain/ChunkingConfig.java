package tech.kayys.wayang.rag.domain;

/**
 * Configuration for document chunking.
 */
public class ChunkingConfig {
    private final ChunkingStrategy strategy;
    private final int chunkSize;
    private final int chunkOverlap;

    public ChunkingConfig(ChunkingStrategy strategy, int chunkSize, int chunkOverlap) {
        this.strategy = strategy;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public static ChunkingConfig defaults() {
        return new ChunkingConfig(ChunkingStrategy.RECURSIVE, 512, 50);
    }

    public ChunkingStrategy strategy() {
        return strategy;
    }

    public int chunkSize() {
        return chunkSize;
    }

    public int chunkOverlap() {
        return chunkOverlap;
    }
}
