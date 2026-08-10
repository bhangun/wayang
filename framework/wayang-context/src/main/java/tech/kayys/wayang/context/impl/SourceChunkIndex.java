package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.model.SourceChunk;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory index of source chunks keyed by file path and line range.
 */
public final class SourceChunkIndex {

    private final Map<Path, List<SourceChunk>> chunksByPath = new LinkedHashMap<>();

    public void add(SourceChunk chunk) {
        Path path = chunk.path().toAbsolutePath().normalize();
        chunksByPath.computeIfAbsent(path, ignored -> new ArrayList<>()).add(chunk);
        chunksByPath.get(path).sort(Comparator.comparingInt(c -> c.range().startLine()));
    }

    public void addAll(List<SourceChunk> chunks) {
        if (chunks == null) return;
        chunks.forEach(this::add);
    }

    public List<SourceChunk> chunksFor(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        return List.copyOf(chunksByPath.getOrDefault(normalized, List.of()));
    }

    public List<SourceChunk> chunksOverlapping(Path path, int startLine, int endLine) {
        if (startLine < 1) throw new IllegalArgumentException("startLine must be >= 1");
        if (endLine < startLine) throw new IllegalArgumentException("endLine must be >= startLine");
        return chunksFor(path).stream()
                .filter(chunk -> chunk.range().startLine() <= endLine && chunk.range().endLine() >= startLine)
                .toList();
    }

    public List<SourceChunk> allChunks() {
        return chunksByPath.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}
