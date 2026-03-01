package tech.kayys.wayang.rag;

import dev.langchain4j.model.embedding.EmbeddingModel;

public interface EmbeddingModelFactory {
    EmbeddingModel createEmbeddingModel(String tenantId, String modelName);
}
