package tech.kayys.wayang.context.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.kayys.wayang.context.api.model.SourceChunk;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineWindowSourceReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readsOverlappingLineWindowsWithoutDuplicatingFinalOverlap() throws Exception {
        Path source = tempDir.resolve("Example.java");
        Files.writeString(source, String.join("\n", List.of("one", "two", "three", "four", "five")));

        LineWindowSourceReader reader = new LineWindowSourceReader(3, 1);

        List<SourceChunk> chunks = reader.read(source);

        assertEquals(2, chunks.size());
        assertEquals(1, chunks.get(0).range().startLine());
        assertEquals(3, chunks.get(0).range().endLine());
        assertEquals("one\ntwo\nthree", chunks.get(0).content());
        assertEquals(3, chunks.get(1).range().startLine());
        assertEquals(5, chunks.get(1).range().endLine());
        assertEquals("three\nfour\nfive", chunks.get(1).content());
    }
}
