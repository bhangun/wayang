package main.java.tech.kayys.wayang.rag;

public interface RetrievalStrategy {
    List<ScoredDocument> retrieve(
            String query,
            EmbeddingStore<TextSegment> store,
            RetrievalConfig config);
}