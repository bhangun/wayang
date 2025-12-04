package tech.kayys.wayang.models.adapter.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIRequest {
    private String model;
    private List<Message> messages;
    private String prompt;
    private List<String> input;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    private Double temperature;
    
    @JsonProperty("top_p")
    private Double topP;
    
    private Integer n;
    private Boolean stream;
    private List<String> stop;
    
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
    
    private List<Function> functions;
    
    @JsonProperty("function_call")
    private Object functionCall;
    
    @JsonProperty("response_format")
    private ResponseFormat responseFormat;
    
    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
        private String name;
        
        @JsonProperty("function_call")
        private FunctionCall functionCall;
    }
    
    @Data
    @Builder
    public static class FunctionCall {
        private String name;
        private String arguments;
    }
    
    @Data
    @Builder
    public static class Function {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }
    
    @Data
    @Builder
    public static class ResponseFormat {
        private String type; // "json_object" or "text"
    }
}