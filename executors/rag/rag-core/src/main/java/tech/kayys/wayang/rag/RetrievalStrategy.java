package tech.kayys.wayang.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.List;

public interface RetrievalStrategy {
    List<ScoredDocument> retrieve(
            String query,
            EmbeddingStore<TextSegment> store,
            RetrievalConfig config);
}