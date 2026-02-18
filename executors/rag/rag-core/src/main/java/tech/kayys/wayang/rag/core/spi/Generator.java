package tech.kayys.wayang.rag.core.spi;

import tech.kayys.wayang.rag.core.model.RagQuery;
import tech.kayys.wayang.rag.core.model.RagScoredChunk;

import java.util.List;

public interface Generator {
    String generate(RagQuery query, List<RagScoredChunk> context);
}
