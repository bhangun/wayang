package tech.kayys.wayang.rag.config;

public class RagValidationErrorResponse {
    private final String code;
    private final String field;
    private final String tenantId;
    private final String value;
    private final String message;

    public RagValidationErrorResponse(String code, String field, String tenantId, String value, String message) {
        this.code = code;
        this.field = field;
        this.tenantId = tenantId;
        this.value = value;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
