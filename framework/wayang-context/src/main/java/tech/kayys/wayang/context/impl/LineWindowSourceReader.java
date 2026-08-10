package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.model.SourceChunk;
import tech.kayys.wayang.context.api.model.SourceChunkMetadata;
import tech.kayys.wayang.context.api.model.SourceChunkPlan;
import tech.kayys.wayang.context.api.model.SourceChunkRange;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Reads source files incrementally into overlapping line windows.
 */
public final class LineWindowSourceReader {

    public static final int DEFAULT_LINES_PER_CHUNK = 200;
    public static final int DEFAULT_OVERLAP_LINES = 20;

    private static final int CHARS_PER_TOKEN = 4;

    private final int linesPerChunk;
    private final int overlapLines;

    public LineWindowSourceReader() {
        this(DEFAULT_LINES_PER_CHUNK, DEFAULT_OVERLAP_LINES);
    }

    public LineWindowSourceReader(int linesPerChunk, int overlapLines) {
        if (linesPerChunk < 1) throw new IllegalArgumentException("linesPerChunk must be >= 1");
        if (overlapLines < 0) throw new IllegalArgumentException("overlapLines must be >= 0");
        if (overlapLines >= linesPerChunk) {
            throw new IllegalArgumentException("overlapLines must be smaller than linesPerChunk");
        }
        this.linesPerChunk = linesPerChunk;
        this.overlapLines = overlapLines;
    }

    public List<SourceChunk> read(Path path) throws IOException {
        Path file = path.toAbsolutePath().normalize();
        List<SourceChunk> chunks = new ArrayList<>();
        List<String> buffer = new ArrayList<>(linesPerChunk);
        int chunkStartLine = 1;
        int lineNumber = 0;
        int lastEmittedEndLine = 0;
        int chunkIndex = 0;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                buffer.add(line);
                if (buffer.size() == linesPerChunk) {
                    chunks.add(toChunk(file, chunkStartLine, lineNumber, chunkIndex++, buffer));
                    lastEmittedEndLine = lineNumber;
                    buffer = retainOverlap(buffer);
                    chunkStartLine = buffer.isEmpty() ? lineNumber + 1 : lineNumber - buffer.size() + 1;
                }
            }
        }

        if (!buffer.isEmpty() && lineNumber > lastEmittedEndLine) {
            chunks.add(toChunk(file, chunkStartLine, lineNumber, chunkIndex, buffer));
        }

        return chunks;
    }

    public SourceChunkPlan plan(Path path) throws IOException {
        List<SourceChunk> chunks = read(path);
        int totalLines = chunks.isEmpty() ? 0 : chunks.get(chunks.size() - 1).range().endLine();
        List<SourceChunkRange> ranges = chunks.stream().map(SourceChunk::range).toList();
        return new SourceChunkPlan(path.toAbsolutePath().normalize(), totalLines,
                linesPerChunk, overlapLines, ranges);
    }

    private List<String> retainOverlap(List<String> buffer) {
        if (overlapLines == 0) return new ArrayList<>();
        int from = Math.max(0, buffer.size() - overlapLines);
        return new ArrayList<>(buffer.subList(from, buffer.size()));
    }

    private SourceChunk toChunk(Path path, int startLine, int endLine, int chunkIndex, List<String> lines) {
        String content = String.join("\n", lines);
        SourceChunkRange range = new SourceChunkRange(startLine, endLine, chunkIndex);
        SourceChunkMetadata metadata = new SourceChunkMetadata(
                path,
                languageFor(path),
                checksum(content),
                estimateTokens(content),
                List.of()
        );
        return new SourceChunk(path, range, content, metadata);
    }

    private String languageFor(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "text";
        return switch (name.substring(dot + 1).toLowerCase()) {
            case "java" -> "java";
            case "kt", "kts" -> "kotlin";
            case "ts", "tsx" -> "typescript";
            case "js", "jsx" -> "javascript";
            case "md" -> "markdown";
            case "xml" -> "xml";
            case "yaml", "yml" -> "yaml";
            default -> "text";
        };
    }

    private long estimateTokens(String content) {
        if (content == null || content.isBlank()) return 0;
        return Math.max(1, content.length() / CHARS_PER_TOKEN);
    }

    private String checksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
