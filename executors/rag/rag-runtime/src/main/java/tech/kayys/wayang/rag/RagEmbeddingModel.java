package tech.kayys.gamelan.executor.rag.langchain;

import java.util.List;

/**
 * Owned embedding model contract for rag-runtime.
 */
public interface RagEmbeddingModel {

    List<float[]> embedAll(List<String> texts);

    int dimension();

    String modelName();
}
