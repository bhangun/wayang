package tech.kayys.wayang.rag;

/**
 * Supported search strategies for RAG retrieval.
 */
public enum SearchStrategy {
    SEMANTIC,
    HYBRID,
    SEMANTIC_RERANK,
    MULTI_QUERY
}
