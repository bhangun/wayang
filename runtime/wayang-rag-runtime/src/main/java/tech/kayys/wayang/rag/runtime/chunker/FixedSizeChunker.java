package tech.kayys.wayang.rag.runtime.chunker;

import tech.kayys.wayang.rag.model.RagChunk;
import tech.kayys.wayang.rag.model.RagDocument;
import tech.kayys.wayang.rag.spi.RagChunker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Splits a document into fixed-size chunks with configurable overlap.
 */
public class FixedSizeChunker implements RagChunker {

    private final int chunkSize;
    private final int overlap;

    public FixedSizeChunker() {
        this(512, 64);
    }

    public FixedSizeChunker(int chunkSize, int overlap) {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize must be > 0");
        if (overlap < 0 || overlap >= chunkSize) throw new IllegalArgumentException("overlap must be in [0, chunkSize)");
        this.chunkSize = chunkSize;
        this.overlap   = overlap;
    }

    @Override
    public List<RagChunk> chunk(RagDocument doc) {
        String text = doc.content();
        if (text == null || text.isBlank()) return List.of();

        List<RagChunk> result = new ArrayList<>();
        int step  = chunkSize - overlap;
        int index = 0;
        int i     = 0;
        while (i < text.length()) {
            int end  = Math.min(i + chunkSize, text.length());
            String slice = text.substring(i, end);
            Map<String, Object> meta = new java.util.LinkedHashMap<>(doc.metadata());
            meta.put("chunkSize", chunkSize);
            meta.put("overlap",   overlap);
            result.add(RagChunk.of(doc.id(), index++, slice, meta));
            if (end == text.length()) break;
            i += step;
        }
        return result;
    }
}
