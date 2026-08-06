package tech.kayys.wayang.error;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.LinkedHashMap;
import java.util.Map;

import tech.kayys.wayang.error.ErrorCode.Severity;

/**
 * Wayang Error Response - Standard error response for APIs.
 * 
 * <p>This class provides a standardized error response format for all
 * Wayang APIs.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * try {
 *     // Some operation
 * } catch (WayangException e) {
 *     return Response.status(400)
 *         .entity(ErrorResponse.from(e))
 *         .build();
 * }
 * }
 * </pre>
 */
public class ErrorResponse {
    
    private final String code;
    private final String message;
    private final String details;
    private final String severity;
    private final String category;
    private final String subsystem;
    private final long timestamp;
    private final String correlationId;
    private final Map<String, Object> context;
    private final String path;
    private final String method;
    
    private ErrorResponse(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.details = builder.details;
        this.severity = builder.severity;
        this.category = builder.category;
        this.subsystem = builder.subsystem;
        this.timestamp = builder.timestamp;
        this.correlationId = builder.correlationId;
        this.context = builder.context;
        this.path = builder.path;
        this.method = builder.method;
    }
    
    /**
     * Creates an error response from an exception.
     *
     * @param e The exception
     * @return The error response
     */
    public static ErrorResponse from(Exception e) {
        if (e instanceof WayangException) {
            WayangException we = (WayangException) e;
            return builder()
                .code(we.getErrorCode().getCode())
                .message(we.getMessage())
                .severity(we.getErrorCode().getSeverity().name())
                .category(we.getErrorCode().getCategory())
                .subsystem(we.getErrorCode().getSubsystem())
                .context(we.getContext())
                .build();
        }
        
        return builder()
            .code(ErrorCode.UNKNOWN_ERROR.getCode())
            .message(e.getMessage() != null ? e.getMessage() : "Unknown error")
            .severity(Severity.ERROR.name())
            .build();
    }
    
    /**
     * Creates a builder for ErrorResponse.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
    public String getSeverity() { return severity; }
    public String getCategory() { return category; }
    public String getSubsystem() { return subsystem; }
    public long getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public Map<String, Object> getContext() { return context; }
    public String getPath() { return path; }
    public String getMethod() { return method; }
    
    public static class Builder {
        private String code;
        private String message;
        private String details;
        private String severity;
        private String category;
        private String subsystem;
        private long timestamp = System.currentTimeMillis();
        private String correlationId;
        private Map<String, Object> context = new LinkedHashMap<>();
        private String path;
        private String method;
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder details(String details) {
            this.details = details;
            return this;
        }
        
        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder subsystem(String subsystem) {
            this.subsystem = subsystem;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public Builder context(Map<String, Object> context) {
            this.context = new LinkedHashMap<>(context);
            return this;
        }
        
        public Builder context(String key, Object value) {
            this.context.put(key, value);
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}