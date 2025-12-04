package tech.kayys.wayang.models.adapter.triton.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TritonInferResponse {
    
    @JsonProperty("model_name")
    private String modelName;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("outputs")
    private List<InferOutput> outputs;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InferOutput {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("shape")
        private List<Long> shape;
        
        @JsonProperty("datatype")
        private String datatype;
        
        @JsonProperty("data")
        private Object data;
    }
}