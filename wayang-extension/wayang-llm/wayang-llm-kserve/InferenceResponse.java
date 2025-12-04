package tech.kayys.wayang.models.kserve.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * KServe V2 Inference Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InferenceResponse {
    
    /**
     * Model name
     */
    @JsonProperty("model_name")
    private String modelName;
    
    /**
     * Model version
     */
    @JsonProperty("model_version")
    private String modelVersion;
    
    /**
     * Request ID (echoed from request)
     */
    @JsonProperty("id")
    private String id;
    
    /**
     * Output tensors
     */
    @JsonProperty("outputs")
    private List<InferOutputTensor> outputs;
    
    /**
     * Response parameters
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /**
     * Output tensor
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InferOutputTensor {
        
        /**
         * Tensor name
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * Tensor shape
         */
        @JsonProperty("shape")
        private List<Long> shape;
        
        /**
         * Data type
         */
        @JsonProperty("datatype")
        private String datatype;
        
        /**
         * Tensor data
         */
        @JsonProperty("data")
        private Object data;
        
        /**
         * Optional parameters
         */
        @JsonProperty("parameters")
        private Map<String, Object> parameters;
    }
}