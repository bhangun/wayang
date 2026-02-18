package tech.kayys.gamelan.executor.rag.langchain;

public record EmbeddingSchemaHistoryCompactionRequest(
        Integer maxEvents,
        Integer maxAgeDays,
        Boolean dryRun) {
}
