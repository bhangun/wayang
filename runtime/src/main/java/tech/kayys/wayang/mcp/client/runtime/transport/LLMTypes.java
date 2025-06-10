package tech.kayys.wayang.mcp.client.runtime.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Common types for LLM requests and responses
 */
public class LLMTypes {
    
    /**
     * Content in a request/response
     */
    public static class Content {
        @JsonProperty("role")
        private final String role;
        
        @JsonProperty("parts")
        private final List<Part> parts;
        
        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }
        
        public String getRole() { return role; }
        public List<Part> getParts() { return parts; }
    }
    
    /**
     * Part of a content
     */
    public static class Part {
        @JsonProperty("text")
        private final String text;
        
        public Part(String text) {
            this.text = text;
        }
        
        public String getText() { return text; }
    }
    
    /**
     * Generation configuration
     */
    public static class GenerationConfig {
        @JsonProperty("temperature")
        private final Double temperature;
        
        @JsonProperty("topK")
        private final Integer topK;
        
        @JsonProperty("topP")
        private final Double topP;
        
        @JsonProperty("maxOutputTokens")
        private final Integer maxOutputTokens;
        
        public GenerationConfig(Double temperature, Integer topK, Double topP, Integer maxOutputTokens) {
            this.temperature = temperature;
            this.topK = topK;
            this.topP = topP;
            this.maxOutputTokens = maxOutputTokens;
        }
        
        public Double getTemperature() { return temperature; }
        public Integer getTopK() { return topK; }
        public Double getTopP() { return topP; }
        public Integer getMaxOutputTokens() { return maxOutputTokens; }
    }
    
    /**
     * Response candidate
     */
    public static class Candidate {
        @JsonProperty("content")
        private final Content content;
        
        @JsonProperty("finishReason")
        private final String finishReason;
        
        @JsonProperty("safetyRatings")
        private final List<SafetyRating> safetyRatings;
        
        public Candidate(Content content, String finishReason, List<SafetyRating> safetyRatings) {
            this.content = content;
            this.finishReason = finishReason;
            this.safetyRatings = safetyRatings;
        }
        
        public Content getContent() { return content; }
        public String getFinishReason() { return finishReason; }
        public List<SafetyRating> getSafetyRatings() { return safetyRatings; }
    }
    
    /**
     * Prompt feedback
     */
    public static class PromptFeedback {
        @JsonProperty("safetyRatings")
        private final List<SafetyRating> safetyRatings;
        
        public PromptFeedback(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }
        
        public List<SafetyRating> getSafetyRatings() { return safetyRatings; }
    }
    
    /**
     * Safety rating
     */
    public static class SafetyRating {
        @JsonProperty("category")
        private final String category;
        
        @JsonProperty("probability")
        private final String probability;
        
        public SafetyRating(String category, String probability) {
            this.category = category;
            this.probability = probability;
        }
        
        public String getCategory() { return category; }
        public String getProbability() { return probability; }
    }
    
    /**
     * LLM request structure
     */
    public static class LLMRequest {
        @JsonProperty("contents")
        private final List<Content> contents;
        
        @JsonProperty("generationConfig")
        private final GenerationConfig generationConfig;
        
        public LLMRequest(List<Content> contents, GenerationConfig generationConfig) {
            this.contents = contents;
            this.generationConfig = generationConfig;
        }
        
        public List<Content> getContents() { return contents; }
        public GenerationConfig getGenerationConfig() { return generationConfig; }
    }
    
    /**
     * LLM response structure
     */
    public static class LLMResponse {
        @JsonProperty("candidates")
        private final List<Candidate> candidates;
        
        @JsonProperty("promptFeedback")
        private final PromptFeedback promptFeedback;
        
        public LLMResponse(List<Candidate> candidates, PromptFeedback promptFeedback) {
            this.candidates = candidates;
            this.promptFeedback = promptFeedback;
        }
        
        public List<Candidate> getCandidates() { return candidates; }
        public PromptFeedback getPromptFeedback() { return promptFeedback; }
    }
    
    /**
     * Create an LLM request
     */
    public static LLMRequest createRequest(String prompt, Double temperature, Integer topK, Double topP, Integer maxTokens) {
        return new LLMRequest(
            List.of(new Content("user", List.of(new Part(prompt)))),
            new GenerationConfig(
                temperature != null ? temperature : 0.7,
                topK != null ? topK : 40,
                topP != null ? topP : 0.95,
                maxTokens != null ? maxTokens : 2048
            )
        );
    }
} 