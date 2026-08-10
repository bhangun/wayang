package tech.kayys.wayang.context.api.model;

import java.nio.file.Path;
import java.util.List;

/**
 * A prompt-ready, line-addressable fragment of a source file.
 */
public record SourceChunk(
        Path path,
        SourceChunkRange range,
        String content,
        SourceChunkMetadata metadata
) {
    public SourceChunk {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (range == null) throw new IllegalArgumentException("range must not be null");
        if (content == null) content = "";
        if (metadata == null) {
            metadata = new SourceChunkMetadata(path, "text", "", 0, List.of());
        }
    }

    public String toPromptContent() {
        return "// source lines " + range.startLine() + "-" + range.endLine()
                + " (chunk " + range.chunkIndex() + ")\n" + content;
    }
}
