package tech.kayys.wayang/models/adapter/triton/dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TritonModelMetadata {
    private String name;
    private List<String> versions;
    private String platform;
    private List<TensorMetadata> inputs;
    private List<TensorMetadata> outputs;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TensorMetadata {
        private String name;
        private String datatype;
        private List<Long> shape;
    }
}