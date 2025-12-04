package tech.kayys.wayang.models.kserve.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * KServe V2 Inference Request.
 * 
 * Specification: https://kserve.github.io/website/docs/concepts/architecture/data-plane/v2-protocol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InferenceRequest {
    
    /**
     * Unique request identifier
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Model name
     */
    @JsonProperty("model_name")
    private String modelName;
    
    /**
     * Model version (optional)
     */
    @JsonProperty("model_version")
    private String modelVersion;
    
    /**
     * Input tensors
     */
    @NotNull
    @Valid
    @JsonProperty("inputs")
    private List<InferInputTensor> inputs;
    
    /**
     * Requested output tensors
     */
    @JsonProperty("outputs")
    private List<InferRequestedOutputTensor> outputs;
    
    /**
     * Request parameters
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /**
     * Input tensor definition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InferInputTensor {
        
        /**
         * Tensor name
         */
        @NotBlank
        @JsonProperty("name")
        private String name;
        
        /**
         * Tensor shape
         */
        @NotNull
        @JsonProperty("shape")
        private List<Long> shape;
        
        /**
         * Data type (e.g., "BYTES", "FP32", "INT64")
         */
        @NotBlank
        @JsonProperty("datatype")
        private String datatype;
        
        /**
         * Tensor data
         */
        @NotNull
        @JsonProperty("data")
        private Object data;
        
        /**
         * Optional parameters
         */
        @JsonProperty("parameters")
        private Map<String, Object> parameters;
    }

    /**
     * Requested output tensor
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InferRequestedOutputTensor {
        
        /**
         * Output tensor name
         */
        @NotBlank
        @JsonProperty("name")
        private String name;
        
        /**
         * Optional parameters
         */
        @JsonProperty("parameters")
        private Map<String, Object> parameters;
    }
}