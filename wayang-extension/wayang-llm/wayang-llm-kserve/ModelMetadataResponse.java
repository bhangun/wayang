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
 * KServe V2 Model Metadata Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelMetadataResponse {
    
    /**
     * Model name
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * Model versions available
     */
    @JsonProperty("versions")
    private List<String> versions;
    
    /**
     * Platform (e.g., "pytorch", "tensorflow")
     */
    @JsonProperty("platform")
    private String platform;
    
    /**
     * Input metadata
     */
    @JsonProperty("inputs")
    private List<TensorMetadata> inputs;
    
    /**
     * Output metadata
     */
    @JsonProperty("outputs")
    private List<TensorMetadata> outputs;
    
    /**
     * Additional parameters
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /**
     * Tensor metadata
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TensorMetadata {
        
        /**
         * Tensor name
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * Data type
         */
        @JsonProperty("datatype")
        private String datatype;
        
        /**
         * Tensor shape (-1 for variable dimension)
         */
        @JsonProperty("shape")
        private List<Long> shape;
        
        /**
         * Optional parameters
         */
        @JsonProperty("parameters")
        private Map<String, Object> parameters;
    }
}