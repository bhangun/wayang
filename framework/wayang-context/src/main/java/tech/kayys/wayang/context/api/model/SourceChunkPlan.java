package tech.kayys.wayang.context.api.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Describes how a source file was divided before context assembly.
 */
public record SourceChunkPlan(
        Path path,
        int totalLines,
        int linesPerChunk,
        int overlapLines,
        List<SourceChunkRange> ranges
) {
    public SourceChunkPlan {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (totalLines < 0) throw new IllegalArgumentException("totalLines must be >= 0");
        if (linesPerChunk < 1) throw new IllegalArgumentException("linesPerChunk must be >= 1");
        if (overlapLines < 0) throw new IllegalArgumentException("overlapLines must be >= 0");
        if (overlapLines >= linesPerChunk) {
            throw new IllegalArgumentException("overlapLines must be smaller than linesPerChunk");
        }
        ranges = ranges == null ? List.of() : List.copyOf(ranges);
    }
}
