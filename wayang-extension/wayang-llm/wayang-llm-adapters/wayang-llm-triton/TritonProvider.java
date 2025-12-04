package tech.kayys.wayang.models.adapter.triton;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.kayys.wayang.models.adapter.triton.client.TritonClient;
import tech.kayys.wayang.models.adapter.triton.dto.TritonInferRequest;
import tech.kayys.wayang.models.adapter.triton.dto.TritonInferResponse;
import tech.kayys.wayang.models.adapter.triton.mapper.TritonMapper;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;
import tech.kayys.wayang.models.api.exception.ModelInferenceException;
import tech.kayys.wayang.models.api.provider.ModelProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Triton Inference Server provider implementation.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class TritonProvider implements ModelProvider {
    
    private static final String PROVIDER_NAME = "triton";
    
    @RestClient
    private final TritonClient client;
    private final TritonMapper mapper;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Timeout(value = 60, unit = ChronoUnit.SECONDS)
    public Uni<ModelResponse> infer(ModelRequest request, String modelId) {
        log.debug("Triton inference: model={}, requestId={}", modelId, request.getRequestId());
        
        TritonInferRequest tritonReq = mapper.toTritonRequest(request, modelId);
        
        return client.infer(modelId, tritonReq)
            .onItem().transform(resp -> mapper.toModelResponse(resp, request.getRequestId(), modelId))
            .onFailure().transform(e -> new ModelInferenceException(
                "Triton inference failed: " + e.getMessage(), e));
    }

    @Override
    public Multi<StreamChunk> inferStream(ModelRequest request, String modelId) {
        // Triton doesn't natively support streaming in the same way
        // Convert to single response
        return Multi.createFrom().uni(infer(request, modelId))
            .onItem().transform(response -> StreamChunk.builder()
                .requestId(request.getRequestId())
                .chunkIndex(0)
                .delta(response.getContent())
                .isFinal(true)
                .finishReason(response.getFinishReason())
                .build());
    }

    @Override
    public Uni<Boolean> healthCheck() {
        return client.healthReady()
            .onItem().transform(TritonClient.TritonHealthResponse::ready)
            .onFailure().recoverWithItem(false);
    }

    @Override
    public Uni<List<String>> getSupportedModels() {
        return client.getServerMetadata()
            .onItem().transform(metadata -> List.of(metadata.getName()));
    }
}