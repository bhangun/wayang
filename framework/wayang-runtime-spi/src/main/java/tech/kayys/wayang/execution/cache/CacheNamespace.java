package tech.kayys.wayang.execution.cache;

/**
 * Namespace discriminates what kind of work the cache is reusing.
 *
 * <ul>
 *   <li>{@code TOOL}      – a tool invocation result (filesystem, shell, API, A2A, etc.)</li>
 *   <li>{@code RETRIEVAL} – a vector / keyword / RAG retrieval result</li>
 *   <li>{@code CONTEXT}   – a compiled prompt-context block</li>
 *   <li>{@code RESEARCH}  – a full research task (query → fetch → extract → summarise)</li>
 *   <li>{@code MODEL}     – a model response for a given prompt hash + parameters</li>
 * </ul>
 */
public enum CacheNamespace {
    TOOL,
    RETRIEVAL,
    CONTEXT,
    RESEARCH,
    MODEL
}
