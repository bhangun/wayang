package tech.kayys.wayang.context.api.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Stable provenance and indexing metadata for a chunk of source code.
 */
public record SourceChunkMetadata(
        Path path,
        String language,
        String checksum,
        long estimatedTokens,
        List<String> symbolHints
) {
    public SourceChunkMetadata {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (language == null || language.isBlank()) language = "text";
        if (checksum == null) checksum = "";
        if (estimatedTokens < 0) throw new IllegalArgumentException("estimatedTokens must be >= 0");
        symbolHints = symbolHints == null ? List.of() : List.copyOf(symbolHints);
    }
}
