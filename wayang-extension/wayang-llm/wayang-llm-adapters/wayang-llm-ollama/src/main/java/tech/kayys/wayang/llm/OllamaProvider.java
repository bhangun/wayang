package tech.kayys.wayang.models.adapter.ollama;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.kayys.wayang.models.adapter.ollama.client.OllamaClient;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaRequest;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaResponse;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaStreamChunk;
import tech.kayys.wayang.models.adapter.ollama.mapper.OllamaMapper;
import tech.kayys.wayang.models.api.dto.ChatMessage;
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
 * Ollama provider implementation.
 * Supports local LLM inference via Ollama.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class OllamaProvider implements ModelProvider {
    
    private static final String PROVIDER_NAME = "ollama";
    
    @RestClient
    private final OllamaClient client;
    private final OllamaMapper mapper;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    public Uni<ModelResponse> infer(ModelRequest request, String modelId) {
        log.debug("Ollama inference: model={}, requestId={}", modelId, request.getRequestId());
        
        OllamaRequest ollamaReq = mapper.toOllamaRequest(request, modelId);
        
        Uni<OllamaResponse> responseUni;
        if ("chat".equalsIgnoreCase(request.getType()) && request.getMessages() != null) {
            responseUni = client.chat(ollamaReq);
        } else {
            responseUni = client.generate(ollamaReq);
        }
        
        return responseUni
            .onItem().transform(resp -> mapper.toModelResponse(resp, request.getRequestId(), modelId))
            .onFailure().transform(e -> new ModelInferenceException(
                "Ollama inference failed: " + e.getMessage(), e));
    }

    @Override
    public Multi<StreamChunk> inferStream(ModelRequest request, String modelId) {
        log.debug("Ollama streaming: model={}, requestId={}", modelId, request.getRequestId());
        
        OllamaRequest ollamaReq = mapper.toOllamaRequest(request, modelId);
        ollamaReq.setStream(true);
        
        Multi<OllamaStreamChunk> chunkStream;
        if ("chat".equalsIgnoreCase(request.getType()) && request.getMessages() != null) {
            chunkStream = client.chatStream(ollamaReq);
        } else {
            chunkStream = client.generateStream(ollamaReq);
        }
        
        AtomicInteger chunkIndex = new AtomicInteger(0);
        
        return chunkStream
            .onItem().transform(chunk -> 
                mapper.toStreamChunk(chunk, request.getRequestId(), chunkIndex.getAndIncrement()))
            .onFailure().invoke(e -> 
                log.error("Ollama streaming failed", e));
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
            .onItem().transform(resp -> resp.models().stream()
                .map(OllamaClient.ModelInfo::name)
                .collect(Collectors.toList()));
    }
}