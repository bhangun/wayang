package tech.kayys.wayang.models.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Streaming response chunk.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamChunk {
    
    /**
     * Matching request identifier
     */
    private String requestId;
    
    /**
     * Chunk index (sequential)
     */
    private Integer chunkIndex;
    
    /**
     * Delta content (incremental text)
     */
    private String delta;
    
    /**
     * Whether this is the final chunk
     */
    @Builder.Default
    private Boolean isFinal = false;
    
    /**
     * Function call delta (if applicable)
     */
    private ChatMessage.FunctionCall functionCallDelta;
    
    /**
     * Finish reason (only in final chunk)
     */
    private String finishReason;
    
    /**
     * Provenance chunk reference
     */
    private String provenanceChunkRef;
    
    /**
     * Error details (if error occurred mid-stream)
     */
    private ModelResponse.ErrorDetails error;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
}