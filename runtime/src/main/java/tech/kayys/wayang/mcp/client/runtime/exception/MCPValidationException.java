package tech.kayys.wayang.mcp.client.runtime.exception;


/**
 * Exception thrown for validation errors
 */
class MCPValidationException extends MCPException {
    private final String field;
    private final Object value;
    
    public MCPValidationException(String message) {
        this(null, null, message);
    }
    
    public MCPValidationException(String field, Object value, String message) {
        super(field != null ? 
            String.format("Validation failed for field '%s' with value '%s': %s", field, value, message) :
            String.format("Validation failed: %s", message));
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
    }
}