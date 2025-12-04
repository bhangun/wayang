package tech.kayys.wayang.models.adapter.vllm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VLLMStreamChunk {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<ChoiceDelta> choices;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChoiceDelta {
        private Integer index;
        private Delta delta;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        private String role;
        private String content;
    }
}