package tech.kayys.wayang.context.impl;

import tech.kayys.wayang.context.api.model.SourceChunk;
import tech.kayys.wayang.context.api.model.SourceChunkPlan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Source-code chunking facade used by context compilers before prompt assembly.
 */
public final class SourceCodeChunker {

    private final LineWindowSourceReader reader;

    public SourceCodeChunker() {
        this(new LineWindowSourceReader());
    }

    public SourceCodeChunker(LineWindowSourceReader reader) {
        if (reader == null) throw new IllegalArgumentException("reader must not be null");
        this.reader = reader;
    }

    public List<SourceChunk> chunk(Path path) {
        try {
            return reader.read(path);
        } catch (IOException e) {
            throw new UncheckedIOException("could not read source chunks from " + path, e);
        }
    }

    public SourceChunkPlan plan(Path path) {
        try {
            return reader.plan(path);
        } catch (IOException e) {
            throw new UncheckedIOException("could not plan source chunks for " + path, e);
        }
    }
}
