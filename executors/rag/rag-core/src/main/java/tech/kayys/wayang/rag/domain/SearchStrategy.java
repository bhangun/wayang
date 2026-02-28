package tech.kayys.wayang.rag.domain;

/**
 * Supported search strategies for RAG retrieval.
 */
public enum SearchStrategy {
    SEMANTIC,
    HYBRID,
    SEMANTIC_RERANK,
    MULTI_QUERY
}
