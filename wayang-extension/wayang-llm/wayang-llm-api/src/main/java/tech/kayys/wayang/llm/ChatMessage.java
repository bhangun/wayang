package tech.kayys.wayang.models.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Single message in a chat conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {
    
    /**
     * Message role: system, user, assistant, function
     */
    @NotBlank
    private String role;
    
    /**
     * Message content (text)
     */
    private String content;
    
    /**
     * Function call (if applicable)
     */
    private FunctionCall functionCall;
    
    /**
     * Function name (for function role messages)
     */
    private String name;
    
    /**
     * Multimodal content (images, audio, etc.)
     */
    private List<ContentPart> contentParts;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionCall {
        private String name;
        private String arguments; // JSON string
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentPart {
        private String type; // text, image_url, audio_url
        private String text;
        private String imageUrl;
        private String audioUrl;
        private Map<String, Object> metadata;
    }
}