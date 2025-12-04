package tech.kayys.wayang.models.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Unified model inference response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelResponse {
    
    /**
     * Matching request identifier
     */
    private String requestId;
    
    /**
     * Model identifier that generated the response
     */
    private String modelId;
    
    /**
     * Response status: ok, error, partial
     */
    private String status;
    
    /**
     * Generated text content
     */
    private String content;
    
    /**
     * Chat messages (for chat responses)
     */
    private List<ChatMessage> messages;
    
    /**
     * Embeddings (for embedding requests)
     */
    private List<List<Double>> embeddings;
    
    /**
     * Function call result (if applicable)
     */
    private ChatMessage.FunctionCall functionCall;
    
    /**
     * Input tokens consumed
     */
    private Integer tokensIn;
    
    /**
     * Output tokens generated
     */
    private Integer tokensOut;
    
    /**
     * Total tokens
     */
    private Integer tokensTotal;
    
    /**
     * Estimated cost (USD)
     */
    private BigDecimal costUsd;
    
    /**
     * Response time (milliseconds)
     */
    private Long latencyMs;
    
    /**
     * Whether response is streaming
     */
    @Builder.Default
    private Boolean streaming = false;
    
    /**
     * Provider-specific trace ID
     */
    private String providerTraceId;
    
    /**
     * Finish reason (stop, length, function_call, etc.)
     */
    private String finishReason;
    
    /**
     * Error details (if status=error)
     */
    private ErrorDetails error;
    
    /**
     * Response timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private String type;
        private Map<String, Object> details;
    }
}