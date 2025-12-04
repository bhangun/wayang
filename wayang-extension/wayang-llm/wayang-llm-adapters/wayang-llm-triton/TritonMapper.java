package tech.kayys.wayang.models.adapter.triton.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.models.adapter.triton.dto.TritonInferRequest;
import tech.kayys.wayang.models.adapter.triton.dto.TritonInferResponse;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps between Wayang and Triton DTOs.
 */
@ApplicationScoped
public class TritonMapper {
    
    public TritonInferRequest toTritonRequest(ModelRequest request, String modelId) {
        List<TritonInferRequest.InferInput> inputs = new ArrayList<>();
        
        // Create text input tensor
        if (request.getPrompt() != null) {
            inputs.add(TritonInferRequest.InferInput.builder()
                .name("input_text")
                .datatype("BYTES")
                .shape(List.of(1L))
                .data(List.of(request.getPrompt()))
                .build());
        }
        
        // Add max_tokens parameter if specified
        if (request.getMaxTokens() != null) {
            inputs.add(TritonInferRequest.InferInput.builder()
                .name("max_tokens")
                .datatype("INT32")
                .shape(List.of(1L))
                .data(List.of(request.getMaxTokens()))
                .build());
        }
        
        return TritonInferRequest.builder()
            .id(request.getRequestId())
            .inputs(inputs)
            .outputs(List.of(
                TritonInferRequest.InferOutput.builder()
                    .name("output_text")
                    .build()
            ))
            .build();
    }
    
    public ModelResponse toModelResponse(TritonInferResponse resp, String requestId, String modelId) {
        ModelResponse.ModelResponseBuilder builder = ModelResponse.builder()
            .requestId(requestId)
            .modelId(modelId)
            .status("ok");
        
        if (resp.getOutputs() != null && !resp.getOutputs().isEmpty()) {
            for (TritonInferResponse.InferOutput output : resp.getOutputs()) {
                if ("output_text".equals(output.getName()) && output.getData() instanceof List) {
                    List<?> data = (List<?>) output.getData();
                    if (!data.isEmpty()) {
                        builder.content(data.get(0).toString());
                    }
                }
            }
        }
        
        return builder.build();
    }
}