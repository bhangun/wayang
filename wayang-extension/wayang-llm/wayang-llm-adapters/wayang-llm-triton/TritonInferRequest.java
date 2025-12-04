package tech.kayys.wayang.models.adapter.triton.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TritonInferRequest {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("inputs")
    private List<InferInput> inputs;
    
    @JsonProperty("outputs")
    private List<InferOutput> outputs;
    
    @Data
    @Builder
    public static class InferInput {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("shape")
        private List<Long> shape;
        
        @JsonProperty("datatype")
        private String datatype;
        
        @JsonProperty("data")
        private Object data;
    }
    
    @Data
    @Builder
    public static class InferOutput {
        @JsonProperty("name")
        private String name;
    }
}