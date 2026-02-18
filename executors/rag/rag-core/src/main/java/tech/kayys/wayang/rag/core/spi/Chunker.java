package tech.kayys.wayang.rag.core.spi;

import tech.kayys.wayang.rag.core.model.RagChunk;
import tech.kayys.wayang.rag.core.model.RagDocument;

import java.util.List;

public interface Chunker {
    List<RagChunk> chunk(RagDocument document, ChunkingOptions options);
}
