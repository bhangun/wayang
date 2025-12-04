package tech.kayys.wayang.models.adapter.vllm;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.kayys.wayang.models.adapter.vllm.client.VLLMClient;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMRequest;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMResponse;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMStreamChunk;
import tech.kayys.wayang.models.adapter.vllm.mapper.VLLMMapper;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;
import tech.kayys.wayang.models.api.exception.ModelInferenceException;
import tech.kayys.wayang.models.api.provider.ModelProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * vLLM provider implementation.
 * vLLM provides OpenAI-compatible API for local model serving.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class VLLMProvider implements ModelProvider {
    
    private static final String PROVIDER_NAME = "vllm";
    
    @RestClient
    private final VLLMClient client;
    private final VLLMMapper mapper;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Timeout(value = 60, unit = ChronoUnit.SECONDS)
    public Uni<ModelResponse> infer(ModelRequest request, String modelId) {
        log.debug("vLLM inference: model={}, requestId={}", modelId, request.getRequestId());
        
        VLLMRequest vllmReq = mapper.toVLLMRequest(request, modelId);
        
        Uni<VLLMResponse> responseUni;
        if ("chat".equalsIgnoreCase(request.getType()) && request.getMessages() != null) {
            responseUni = client.chatCompletion(vllmReq);
        } else {
            responseUni = client.completion(vllmReq);
        }
        
        return responseUni
            .onItem().transform(resp -> mapper.toModelResponse(resp, request.getRequestId(), modelId))
            .onFailure().transform(e -> new ModelInferenceException(
                "vLLM inference failed: " + e.getMessage(), e));
    }

    @Override
    public Multi<StreamChunk> inferStream(ModelRequest request, String modelId) {
        log.debug("vLLM streaming: model={}, requestId={}", modelId, request.getRequestId());
        
        VLLMRequest vllmReq = mapper.toVLLMRequest(request, modelId);
        vllmReq.setStream(true);
        
        Multi<VLLMStreamChunk> chunkStream;
        if ("chat".equalsIgnoreCase(request.getType()) && request.getMessages() != null) {
            chunkStream = client.chatCompletionStream(vllmReq);
        } else {
            chunkStream = client.completionStream(vllmReq);
        }
        
        AtomicInteger chunkIndex = new AtomicInteger(0);
        
        return chunkStream
            .onItem().transform(chunk -> 
                mapper.toStreamChunk(chunk, request.getRequestId(), chunkIndex.getAndIncrement()))
            .onFailure().invoke(e -> 
                log.error("vLLM streaming failed", e));
    }

    @Override
    public Uni<Boolean> healthCheck() {
        return client.listModels()
            .onItem().transform(resp -> true)
            .onFailure().recoverWithItem(false);
    }

    @Override
    public Uni<List<String>> getSupportedModels() {
        return client.listModels()
            .onItem().transform(resp -> resp.data().stream()
                .map(VLLMClient.ModelInfo::id)
                .collect(Collectors.toList()));
    }
}