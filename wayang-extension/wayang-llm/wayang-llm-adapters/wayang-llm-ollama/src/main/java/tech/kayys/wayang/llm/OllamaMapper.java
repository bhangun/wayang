package tech.kayys.wayang.models.adapter.ollama.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaRequest;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaResponse;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaStreamChunk;
import tech.kayys.wayang.models.api.dto.ChatMessage;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between Wayang and Ollama DTOs.
 */
@ApplicationScoped
public class OllamaMapper {
    
    public OllamaRequest toOllamaRequest(ModelRequest request, String modelId) {
        OllamaRequest.OllamaRequestBuilder builder = OllamaRequest.builder()
            .model(modelId)
            .stream(Boolean.TRUE.equals(request.getStream()));
        
        // Set messages or prompt
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            builder.messages(request.getMessages().stream()
                .map(this::toOllamaMessage)
                .collect(Collectors.toList()));
        } else if (request.getPrompt() != null) {
            builder.prompt(request.getPrompt());
        }
        
        // Set options
        OllamaRequest.Options options = OllamaRequest.Options.builder()
            .temperature(request.getTemperature())
            .topK(request.getTopK())
            .topP(request.getTopP())
            .numPredict(request.getMaxTokens())
            .stop(request.getStop())
            .build();
        builder.options(options);
        
        return builder.build();
    }
    
    public ModelResponse toModelResponse(OllamaResponse resp, String requestId, String modelId) {
        String content = resp.getResponse() != null ? resp.getResponse() : 
            (resp.getMessage() != null ? resp.getMessage().getContent() : null);
        
        return ModelResponse.builder()
            .requestId(requestId)
            .modelId(modelId)
            .status("ok")
            .content(content)
            .tokensIn(resp.getPromptEvalCount())
            .tokensOut(resp.getEvalCount())
            .tokensTotal((resp.getPromptEvalCount() != null ? resp.getPromptEvalCount() : 0) +
                        (resp.getEvalCount() != null ? resp.getEvalCount() : 0))
            .latencyMs(resp.getTotalDuration() != null ? resp.getTotalDuration() / 1_000_000 : null)
            .finishReason("stop")
            .build();
    }
    
    public StreamChunk toStreamChunk(OllamaStreamChunk chunk, String requestId, int index) {
        String delta = chunk.getResponse() != null ? chunk.getResponse() :
            (chunk.getMessage() != null ? chunk.getMessage().getContent() : "");
        
        return StreamChunk.builder()
            .requestId(requestId)
            .chunkIndex(index)
            .delta(delta)
            .isFinal(Boolean.TRUE.equals(chunk.getDone()))
            .finishReason(Boolean.TRUE.equals(chunk.getDone()) ? "stop" : null)
            .build();
    }
    
    private OllamaRequest.Message toOllamaMessage(ChatMessage msg) {
        return OllamaRequest.Message.builder()
            .role(msg.getRole())
            .content(msg.getContent())
            .build();
    }
}