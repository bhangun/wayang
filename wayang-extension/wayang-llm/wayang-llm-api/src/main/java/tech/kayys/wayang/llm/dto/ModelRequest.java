package tech.kayys.wayang.models.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.kayys.wayang.models.api.domain.ModelCapability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unified model inference request.
 * Supports chat, completion, embedding, and multimodal requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelRequest {
    
    /**
     * Unique request identifier
     */
    @NotBlank
    private String requestId;
    
    /**
     * Tenant identifier
     */
    @NotBlank
    private String tenantId;
    
    /**
     * Workflow run identifier
     */
    private String runId;
    
    /**
     * Node identifier (if from workflow)
     */
    private String nodeId;
    
    /**
     * Request type: chat, completion, embed, multimodal
     */
    @NotBlank
    private String type;
    
    /**
     * Model hints for routing
     */
    private ModelHints modelHints;
    
    /**
     * Chat messages (for chat type)
     */
    @Valid
    private List<ChatMessage> messages;
    
    /**
     * Prompt text (for completion type)
     */
    private String prompt;
    
    /**
     * Input texts (for embedding type)
     */
    private List<String> inputs;
    
    /**
     * Function/tool definitions
     */
    private List<FunctionDefinition> functions;
    
    /**
     * Whether to stream response
     */
    @Builder.Default
    private Boolean stream = false;
    
    /**
     * Request timeout (milliseconds)
     */
    @Positive
    @Builder.Default
    private Integer timeoutMs = 30000;
    
    /**
     * Maximum tokens to generate
     */
    @Positive
    private Integer maxTokens;
    
    /**
     * Temperature (0.0 - 2.0)
     */
    private Double temperature;
    
    /**
     * Top-P sampling
     */
    private Double topP;
    
    /**
     * Top-K sampling
     */
    private Integer topK;
    
    /**
     * Stop sequences
     */
    private List<String> stop;
    
    /**
     * Presence penalty
     */
    private Double presencePenalty;
    
    /**
     * Frequency penalty
     */
    private Double frequencyPenalty;
    
    /**
     * Additional request metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Trace identifier for observability
     */
    private String traceId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelHints {
        /**
         * Required capabilities
         */
        private Set<ModelCapability> capabilities;
        
        /**
         * Preferred model IDs (in priority order)
         */
        private List<String> preferred;
        
        /**
         * Maximum latency budget (ms)
         */
        private Integer maxLatencyMs;
        
        /**
         * Maximum cost budget (USD)
         */
        private Double maxCostUsd;
        
        /**
         * Required quality level (0.0 - 1.0)
         */
        private Double minQuality;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionDefinition {
        @NotBlank
        private String name;
        
        private String description;
        
        @NotNull
        private Map<String, Object> parameters; // JSON Schema
    }
}