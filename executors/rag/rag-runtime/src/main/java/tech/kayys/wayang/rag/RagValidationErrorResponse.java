package tech.kayys.wayang.rag;

public record RagValidationErrorResponse(
        String code,
        String field,
        String tenantId,
        String value,
        String message) {
}
