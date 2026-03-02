package tech.kayys.wayang.rag.runtime;
import tech.kayys.wayang.rag.plugin.api.*;
import tech.kayys.wayang.rag.core.*;

public record RagValidationErrorResponse(
        String code,
        String field,
        String tenantId,
        String value,
        String message) {
}
