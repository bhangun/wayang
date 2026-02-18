package tech.kayys.gamelan.executor.rag.langchain;

public record RagValidationErrorResponse(
        String code,
        String field,
        String tenantId,
        String value,
        String message) {
}
