package tech.kayys.gamelan.executor.rag.langchain;

public record EmbeddingSchemaMigrationRequest(
        String tenantId,
        String embeddingModel,
        Integer embeddingDimension,
        String embeddingVersion,
        Boolean clearNamespace,
        Boolean dryRun) {
}
