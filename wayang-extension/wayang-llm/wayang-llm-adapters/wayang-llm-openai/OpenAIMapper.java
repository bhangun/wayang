package tech.kayys.wayang.models.adapter.openai.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIRequest;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIResponse;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIStreamChunk;
import tech.kayys.wayang.models.api.dto.ChatMessage;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps between Wayang and OpenAI DTOs.
 */
@ApplicationScoped
public class OpenAIMapper {
    
    public OpenAIRequest toOpenAIRequest(ModelRequest request, String modelId) {
        OpenAIRequest.OpenAIRequestBuilder builder = OpenAIRequest.builder()
            .model(modelId)
            .maxTokens(request.getMaxTokens())
            .temperature(request.getTemperature())
            .topP(request.getTopP())
            .stop(request.getStop())
            .presencePenalty(request.getPresencePenalty())
            .frequencyPenalty(request.getFrequencyPenalty())
            .stream(Boolean.TRUE.equals(request.getStream()));
        
        // Map messages or prompt
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            builder.messages(request.getMessages().stream()
                .map(this::toOpenAIMessage)
                .collect(Collectors.toList()));
        } else if (request.getPrompt() != null) {
            builder.prompt(request.getPrompt());
        } else if (request.getInputs() != null) {
            builder.input(request.getInputs());
        }
        
        // Map functions
        if (request.getFunctions() != null && !request.getFunctions().isEmpty()) {
            builder.functions(request.getFunctions().stream()
                .map(this::toOpenAIFunction)
                .collect(Collectors.toList()));
        }
        
        return builder.build();
    }
    
    public ModelResponse toModelResponse(OpenAIResponse resp, String requestId, String modelId) {
        ModelResponse.ModelResponseBuilder builder = ModelResponse.builder()
            .requestId(requestId)
            .modelId(modelId)
            .status("ok");
        
        // Handle chat/completion response
        if (resp.getChoices() != null && !resp.getChoices().isEmpty()) {
            OpenAIResponse.Choice choice = resp.getChoices().get(0);
            
            if (choice.getMessage() != null) {
                builder.content(choice.getMessage().getContent());
                
                if (choice.getMessage().getFunctionCall() != null) {
                    builder.functionCall(new ChatMessage.FunctionCall(
                        choice.getMessage().getFunctionCall().getName(),
                        choice.getMessage().getFunctionCall().getArguments()
                    ));
                }
            } else if (choice.getText() != null) {
                builder.content(choice.getText());
            }
            
            builder.finishReason(choice.getFinishReason());
        }
        
        // Handle embeddings
        if (resp.getData() != null && !resp.getData().isEmpty()) {
            List<List<Double>> embeddings = resp.getData().stream()
                .map(OpenAIResponse.EmbeddingData::getEmbedding)
                .collect(Collectors.toList());
            builder.embeddings(embeddings);
        }
        
        // Usage
        if (resp.getUsage() != null) {
            builder.tokensIn(resp.getUsage().getPromptTokens())
                .tokensOut(resp.getUsage().getCompletionTokens())
                .tokensTotal(resp.getUsage().getTotalTokens());
        }
        
        return builder.build();
    }
    
    public StreamChunk toStreamChunk(OpenAIStreamChunk chunk, String requestId, int index) {
        if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
            return StreamChunk.builder()
                .requestId(requestId)
                .chunkIndex(index)
                .delta("")
                .isFinal(false)
                .build();
        }
        
        OpenAIStreamChunk.ChoiceDelta choice = chunk.getChoices().get(0);
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
    
    private OpenAIRequest.Message toOpenAIMessage(ChatMessage msg) {
        OpenAIRequest.Message.MessageBuilder builder = OpenAIRequest.Message.builder()
            .role(msg.getRole())
            .content(msg.getContent())
            .name(msg.getName());
        
        if (msg.getFunctionCall() != null) {
            builder.functionCall(OpenAIRequest.FunctionCall.builder()
                .name(msg.getFunctionCall().getName())
                .arguments(msg.getFunctionCall().getArguments())
                .build());
        }
        
        return builder.build();
    }
    
    private OpenAIRequest.Function toOpenAIFunction(ModelRequest.FunctionDefinition func) {
        return OpenAIRequest.Function.builder()
            .name(func.getName())
            .description(func.getDescription())
            .parameters(func.getParameters())
            .build();
    }
}