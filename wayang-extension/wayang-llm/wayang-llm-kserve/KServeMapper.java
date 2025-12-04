package tech.kayys.wayang.models.kserve.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.dto.ChatMessage;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.kserve.dto.InferenceRequest;
import tech.kayys.wayang.models.kserve.dto.InferenceResponse;

import java.util.*;

/**
 * Maps between KServe V2 protocol and Wayang internal DTOs.
 */
@ApplicationScoped
@Slf4j
public class KServeMapper {
    
    private static final String INPUT_TENSOR_PROMPT = "prompt";
    private static final String INPUT_TENSOR_MESSAGES = "messages";
    private static final String OUTPUT_TENSOR_TEXT = "text_output";
    private static final String OUTPUT_TENSOR_TOKENS = "token_count";
    
    /**
     * Convert KServe inference request to internal ModelRequest.
     */
    public ModelRequest toModelRequest(InferenceRequest request) {
        ModelRequest.ModelRequestBuilder builder = ModelRequest.builder()
            .requestId(request.getId() != null ? request.getId() : UUID.randomUUID().toString())
            .tenantId(extractTenantId(request))
            .type(extractRequestType(request));
        
        // Extract prompt or messages from inputs
        for (InferenceRequest.InferInputTensor input : request.getInputs()) {
            if (INPUT_TENSOR_PROMPT.equals(input.getName())) {
                builder.prompt(extractString(input.getData()));
            } else if (INPUT_TENSOR_MESSAGES.equals(input.getName())) {
                builder.messages(extractMessages(input.getData()));
            }
        }
        
        // Extract parameters
        if (request.getParameters() != null) {
            extractParameters(request.getParameters(), builder);
        }
        
        // Set model hints
        if (request.getModelName() != null) {
            builder.modelHints(ModelRequest.ModelHints.builder()
                .preferred(List.of(request.getModelName()))
                .build());
        }
        
        return builder.build();
    }
    
    /**
     * Convert internal ModelResponse to KServe inference response.
     */
    public InferenceResponse toInferenceResponse(ModelResponse response, String modelName) {
        List<InferenceResponse.InferOutputTensor> outputs = new ArrayList<>();
        
        // Text output
        if (response.getContent() != null) {
            outputs.add(InferenceResponse.InferOutputTensor.builder()
                .name(OUTPUT_TENSOR_TEXT)
                .datatype("BYTES")
                .shape(List.of(1L))
                .data(List.of(response.getContent()))
                .build());
        }
        
        // Token count output
        if (response.getTokensTotal() != null) {
            outputs.add(InferenceResponse.InferOutputTensor.builder()
                .name(OUTPUT_TENSOR_TOKENS)
                .datatype("INT64")
                .shape(List.of(1L))
                .data(List.of(response.getTokensTotal()))
                .build());
        }
        
        // Build parameters
        Map<String, Object> parameters = new HashMap<>();
        if (response.getFinishReason() != null) {
            parameters.put("finish_reason", response.getFinishReason());
        }
        if (response.getTokensIn() != null) {
            parameters.put("tokens_in", response.getTokensIn());
        }
        if (response.getTokensOut() != null) {
            parameters.put("tokens_out", response.getTokensOut());
        }
        if (response.getLatencyMs() != null) {
            parameters.put("latency_ms", response.getLatencyMs());
        }
        if (response.getCostUsd() != null) {
            parameters.put("cost_usd", response.getCostUsd());
        }
        
        return InferenceResponse.builder()
            .id(response.getRequestId())
            .modelName(modelName)
            .modelVersion(extractVersion(response.getModelId()))
            .outputs(outputs)
            .parameters(parameters.isEmpty() ? null : parameters)
            .build();
    }
    
    private String extractTenantId(InferenceRequest request) {
        if (request.getParameters() != null && request.getParameters().containsKey("tenant_id")) {
            return request.getParameters().get("tenant_id").toString();
        }
        return "default";
    }
    
    private String extractRequestType(InferenceRequest request) {
        if (request.getParameters() != null && request.getParameters().containsKey("type")) {
            return request.getParameters().get("type").toString();
        }
        
        // Infer type from inputs
        for (InferenceRequest.InferInputTensor input : request.getInputs()) {
            if (INPUT_TENSOR_MESSAGES.equals(input.getName())) {
                return "chat";
            }
        }
        
        return "completion";
    }
    
    private String extractString(Object data) {
        if (data instanceof String) {
            return (String) data;
        } else if (data instanceof List) {
            List<?> list = (List<?>) data;
            if (!list.isEmpty() && list.get(0) instanceof String) {
                return (String) list.get(0);
            }
        }
        return data.toString();
    }
    
    @SuppressWarnings("unchecked")
    private List<ChatMessage> extractMessages(Object data) {
        if (!(data instanceof List)) {
            return List.of();
        }
        
        List<?> messageList = (List<?>) data;
        List<ChatMessage> messages = new ArrayList<>();
        
        for (Object msgObj : messageList) {
            if (msgObj instanceof Map) {
                Map<String, Object> msgMap = (Map<String, Object>) msgObj;
                messages.add(ChatMessage.builder()
                    .role(msgMap.get("role").toString())
                    .content(msgMap.get("content").toString())
                    .build());
            } else if (msgObj instanceof String) {
                // Try to parse as JSON
                // For simplicity, treat as user message
                messages.add(ChatMessage.builder()
                    .role("user")
                    .content(msgObj.toString())
                    .build());
            }
        }
        
        return messages;
    }
    
    private void extractParameters(Map<String, Object> params, ModelRequest.ModelRequestBuilder builder) {
        if (params.containsKey("max_tokens")) {
            builder.maxTokens(((Number) params.get("max_tokens")).intValue());
        }
        if (params.containsKey("temperature")) {
            builder.temperature(((Number) params.get("temperature")).doubleValue());
        }
        if (params.containsKey("top_p")) {
            builder.topP(((Number) params.get("top_p")).doubleValue());
        }
        if (params.containsKey("top_k")) {
            builder.topK(((Number) params.get("top_k")).intValue());
        }
        if (params.containsKey("stream")) {
            builder.stream((Boolean) params.get("stream"));
        }
        if (params.containsKey("timeout_ms")) {
            builder.timeoutMs(((Number) params.get("timeout_ms")).intValue());
        }
    }
    
    private String extractVersion(String modelId) {
        if (modelId == null) {
            return "1";
        }
        // Try to extract version from model ID (e.g., "gpt-4-1.0" -> "1.0")
        int lastDash = modelId.lastIndexOf('-');
        if (lastDash > 0 && lastDash < modelId.length() - 1) {
            String potentialVersion = modelId.substring(lastDash + 1);
            if (potentialVersion.matches("\\d+(\\.\\d+)*")) {
                return potentialVersion;
            }
        }
        return "1";
    }
}