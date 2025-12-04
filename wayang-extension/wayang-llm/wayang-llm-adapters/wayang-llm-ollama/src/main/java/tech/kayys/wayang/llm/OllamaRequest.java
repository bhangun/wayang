package tech.kayys.wayang.models.adapter.ollama.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaRequest {
    private String model;
    private String prompt;
    private List<Message> messages;
    private Boolean stream;
    private Options options;
    private String format; // "json" for JSON mode
    
    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
    
    @Data
    @Builder
    public static class Options {
        private Double temperature;
        private Integer topK;
        private Double topP;
        private Integer numPredict; // max tokens
        private List<String> stop;
    }
}