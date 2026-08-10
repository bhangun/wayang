package tech.kayys.wayang.context.api.model;

/**
 * Identifies the exact line window represented by a source chunk.
 */
public record SourceChunkRange(int startLine, int endLine, int chunkIndex) {
    public SourceChunkRange {
        if (startLine < 1) throw new IllegalArgumentException("startLine must be >= 1");
        if (endLine < startLine) throw new IllegalArgumentException("endLine must be >= startLine");
        if (chunkIndex < 0) throw new IllegalArgumentException("chunkIndex must be >= 0");
    }

    public int lineCount() {
        return endLine - startLine + 1;
    }
}
