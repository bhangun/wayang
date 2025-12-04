package tech.kayys.wayang.models.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Complete metadata for a registered model.
 * Stored in Model Registry and used for routing decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelMetadata {
    
    /**
     * Unique model identifier (e.g., "gpt-4", "llama-3-70b")
     */
    @NotBlank
    private String modelId;
    
    /**
     * Human-readable name
     */
    @NotBlank
    private String name;
    
    /**
     * Model version
     */
    @NotBlank
    private String version;
    
    /**
     * Provider identifier (e.g., "openai", "ollama", "vllm")
     */
    @NotBlank
    private String provider;
    
    /**
     * Model type
     */
    @NotNull
    private ModelType type;
    
    /**
     * Supported capabilities
     */
    @Builder.Default
    private Set<ModelCapability> capabilities = Set.of();
    
    /**
     * Maximum context window in tokens
     */
    @Positive
    private Integer maxTokens;
    
    /**
     * Maximum output tokens
     */
    private Integer maxOutputTokens;
    
    /**
     * Latency profile (P50, P95, P99 in milliseconds)
     */
    private LatencyProfile latencyProfile;
    
    /**
     * Cost profile
     */
    private CostProfile costProfile;
    
    /**
     * Supported languages (ISO codes)
     */
    private List<String> supportedLanguages;
    
    /**
     * Model description
     */
    private String description;
    
    /**
     * Tags for categorization
     */
    private Set<String> tags;
    
    /**
     * Custom attributes
     */
    private Map<String, Object> attributes;
    
    /**
     * Deployment endpoint URL (if applicable)
     */
    private String endpoint;
    
    /**
     * Model status (active, deprecated, experimental)
     */
    @Builder.Default
    private ModelStatus status = ModelStatus.ACTIVE;
    
    /**
     * Registration timestamp
     */
    private Instant createdAt;
    
    /**
     * Last updated timestamp
     */
    private Instant updatedAt;
    
    /**
     * Model owner/team
     */
    private String owner;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatencyProfile {
        private Integer p50Ms;
        private Integer p95Ms;
        private Integer p99Ms;
        private Integer avgMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostProfile {
        /**
         * Cost per input token (USD)
         */
        private BigDecimal perInputToken;
        
        /**
         * Cost per output token (USD)
         */
        private BigDecimal perOutputToken;
        
        /**
         * Cost per request (USD)
         */
        private BigDecimal perRequest;
        
        /**
         * Cost per embedding (USD)
         */
        private BigDecimal perEmbedding;
    }

    public enum ModelStatus {
        ACTIVE,
        DEPRECATED,
        EXPERIMENTAL,
        DISABLED
    }
}