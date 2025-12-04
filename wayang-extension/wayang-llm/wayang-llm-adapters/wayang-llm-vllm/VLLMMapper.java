package tech.kayys.wayang.models.adapter.vllm.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMRequest;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMResponse;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMStreamChunk;
import tech.kayys.wayang.models.api.dto.ChatMessage;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;

import java.util.stream.Collectors;

/**
 * Maps between Wayang and vLLM DTOs.
 */
@ApplicationScoped
public class VLLMMapper {
    
    public VLLMRequest toVLLMRequest(ModelRequest request, String modelId) {
        VLLMRequest.VLLMRequestBuilder builder = VLLMRequest.builder()
            .model(modelId)
            .maxTokens(request.getMaxTokens())
            .temperature(request.getTemperature())
            .topP(request.getTopP())
            .topK(request.getTopK())
            .stop(request.getStop())
            .presencePenalty(request.getPresencePenalty())
            .frequencyPenalty(request.getFrequencyPenalty())
            .stream(Boolean.TRUE.equals(request.getStream()));
        
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            builder.messages(request.getMessages().stream()
                .map(this::toVLLMMessage)
                .collect(Collectors.toList()));
        } else if (request.getPrompt() != null) {
            builder.prompt(request.getPrompt());
        }
        
        return builder.build();
    }
    
    public ModelResponse toModelResponse(VLLMResponse resp, String requestId, String modelId) {
        ModelResponse.ModelResponseBuilder builder = ModelResponse.builder()
            .requestId(requestId)
            .modelId(modelId)
            .status("ok");
        
        if (resp.getChoices() != null && !resp.getChoices().isEmpty()) {
            VLLMResponse.Choice choice = resp.getChoices().get(0);
            
            if (choice.getMessage() != null) {
                builder.content(choice.getMessage().getContent());
            } else if (choice.getText() != null) {
                builder.content(choice.getText());
            }
            
            builder.finishReason(choice.getFinishReason());
        }
        
        if (resp.getUsage() != null) {
            builder.tokensIn(resp.getUsage().getPromptTokens())
                .tokensOut(resp.getUsage().getCompletionTokens())
                .tokensTotal(resp.getUsage().getTotalTokens());
        }
        
        return builder.build();
    }
    
    public StreamChunk toStreamChunk(VLLMStreamChunk chunk, String requestId, int index) {
        if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
            return StreamChunk.builder()
                .requestId(requestId)
                .chunkIndex(index)
                .delta("")
                .isFinal(false)
                .build();
        }
        
        VLLMStreamChunk.ChoiceDelta choice = chunk.getChoices().get(0);
        String delta = choice.getDelta() != null && choice.getDelta().getContent() != null ?
            choice.getDelta().getContent() : "";
        
        return StreamChunk.builder()
            .requestId(requestId)
            .chunkIndex(index)
            .delta(delta)
            .isFinal(choice.getFinishReason() != null)
            .finishReason(choice.getFinishReason())
            .build();
    }
    
    private VLLMRequest.Message toVLLMMessage(ChatMessage msg) {
        return VLLMRequest.Message.builder()
            .role(msg.getRole())
            .content(msg.getContent())
            .build();
    }
}