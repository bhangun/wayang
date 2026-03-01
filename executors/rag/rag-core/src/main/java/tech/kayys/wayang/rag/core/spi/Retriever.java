package tech.kayys.wayang.rag.core.spi;

import tech.kayys.wayang.rag.RagQuery;
import tech.kayys.wayang.rag.RagScoredChunk;

import java.util.List;

public interface Retriever {
    List<RagScoredChunk> retrieve(RagQuery query);
}
