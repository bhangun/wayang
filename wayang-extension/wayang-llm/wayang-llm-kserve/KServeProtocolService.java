package tech.kayys.wayang.models.kserve.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.service.ModelRegistry;
import tech.kayys.wayang.models.api.service.ModelService;
import tech.kayys.wayang.models.kserve.dto.*;
import tech.kayys.wayang.models.kserve.mapper.KServeMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KServe V2 protocol service implementation.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class KServeProtocolService {
    
    private final ModelService modelService;
    private final ModelRegistry modelRegistry;
    private final KServeMapper mapper;
    
    private static final String SERVER_NAME = "wayang-models";
    private static final String SERVER_VERSION = "1.0.0";
    
    /**
     * Handle KServe inference request.
     */
    public Uni<InferenceResponse> infer(InferenceRequest request) {
        log.info("KServe inference request: id={}, model={}", 
            request.getId(), request.getModelName());
        
        // Convert to internal format
        ModelRequest modelRequest = mapper.toModelRequest(request);
        
        // Execute inference
        return modelService.infer(modelRequest)
            .onItem().transform(response -> 
                mapper.toInferenceResponse(response, request.getModelName()));
    }
    
    /**
     * Get server metadata.
     */
    public Uni<ServerMetadataResponse> getServerMetadata() {
        return Uni.createFrom().item(ServerMetadataResponse.builder()
            .name(SERVER_NAME)
            .version(SERVER_VERSION)
            .extensions(List.of("text-generation", "embeddings", "chat"))
            .build());
    }
    
    /**
     * Check if server is ready.
     */
    public Uni<Boolean> isServerReady() {
        return modelService.healthCheck();
    }
    
    /**
     * Check if server is alive.
     */
    public Uni<Boolean> isServerAlive() {
        return Uni.createFrom().item(true);
    }
    
    /**
     * Get model metadata.
     */
    public Uni<ModelMetadataResponse> getModelMetadata(String modelName, String modelVersion) {
        String modelId = modelVersion != null ? 
            modelName + "-" + modelVersion : modelName;
        
        return modelRegistry.getModel(modelId)
            .onItem().transform(optModel -> optModel
                .map(this::toKServeMetadata)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException(
                    "Model not found: " + modelName)));
    }
    
    /**
     * Check if model is ready.
     */
    public Uni<ModelReadyResponse> isModelReady(String modelName, String modelVersion) {
        String modelId = modelVersion != null ? 
            modelName + "-" + modelVersion : modelName;
        
        return modelRegistry.getModel(modelId)
            .onItem().transform(optModel -> ModelReadyResponse.builder()
                .name(modelName)
                .ready(optModel.isPresent() && 
                    optModel.get().getStatus() == ModelMetadata.ModelStatus.ACTIVE)
                .build());
    }
    
    private ModelMetadataResponse toKServeMetadata(ModelMetadata model) {
        // Create input metadata (text input)
        List<ModelMetadataResponse.TensorMetadata> inputs = List.of(
            ModelMetadataResponse.TensorMetadata.builder()
                .name("prompt")
                .datatype("BYTES")
                .shape(List.of(-1L)) // Variable length
                .build(),
            ModelMetadataResponse.TensorMetadata.builder()
                .name("messages")
                .datatype("BYTES")
                .shape(List.of(-1L, -1L)) // Variable number of messages
                .build()
        );
        
        // Create output metadata
        List<ModelMetadataResponse.TensorMetadata> outputs = List.of(
            ModelMetadataResponse.TensorMetadata.builder()
                .name("text_output")
                .datatype("BYTES")
                .shape(List.of(-1L))
                .build(),
            ModelMetadataResponse.TensorMetadata.builder()
                .name("token_count")
                .datatype("INT64")
                .shape(List.of(1L))
                .build()
        );
        
        // Build parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_tokens", model.getMaxTokens());
        if (model.getCapabilities() != null) {
            parameters.put("capabilities", model.getCapabilities().stream()
                .map(Enum::name)
                .collect(Collectors.toList()));
        }
        
        return ModelMetadataResponse.builder()
            .name(model.getModelId())
            .versions(List.of(model.getVersion()))
            .platform(model.getProvider())
            .inputs(inputs)
            .outputs(outputs)
            .parameters(parameters)
            .build();
    }
}