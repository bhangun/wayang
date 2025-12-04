package tech.kayys.wayang.models.adapter.openai;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.kayys.wayang.models.adapter.openai.client.OpenAIClient;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIRequest;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIResponse;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIStreamChunk;
import tech.kayys.wayang.models.adapter.openai.mapper.OpenAIMapper;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;
import tech.kayys.wayang.models.api.exception.ModelInferenceException;
import tech.kayys.wayang.models.api.provider.ModelProvider;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OpenAI provider implementation.
 * Supports GPT models, embeddings, and function calling.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class OpenAIProvider implements ModelProvider {
    
    private static final String PROVIDER_NAME = "openai";
    private static final List<String> SUPPORTED_MODELS = List.of(
        "gpt-4", "gpt-4-turbo", "gpt-3.5-turbo", "text-embedding-ada-002"
    );
    
    @RestClient
    private final OpenAIClient client;
    private final OpenAIMapper mapper;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    @Retry(maxRetries = 3, delay = 1000, delayUnit = ChronoUnit.MILLIS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 10000)
    @Timeout(value = 60, unit = ChronoUnit.SECONDS)
    public Uni<ModelResponse> infer(ModelRequest request, String modelId) {
        log.debug("OpenAI inference: model={}, requestId={}", modelId, request.getRequestId());
        
        OpenAIRequest openaiReq = mapper.toOpenAIRequest(request, modelId);
        
        Uni<OpenAIResponse> responseUni;
        if ("embed".equalsIgnoreCase(request.getType()) || 
            "embedding".equalsIgnoreCase(request.getType())) {
            responseUni = client.embeddings(openaiReq);
        } else if ("chat".equalsIgnoreCase(request.getType())) {
            responseUni = client.chatCompletion(openaiReq);
        } else {
            responseUni = client.completion(openaiReq);
        }
        
        return responseUni
            .onItem().transform(resp -> mapper.toModelResponse(resp, request.getRequestId(), modelId))
            .onFailure().transform(e -> new ModelInferenceException(
                "OpenAI inference failed: " + e.getMessage(), e));
    }

    @Override
    public Multi<StreamChunk> inferStream(ModelRequest request, String modelId) {
        log.debug("OpenAI streaming: model={}, requestId={}", modelId, request.getRequestId());
        
        OpenAIRequest openaiReq = mapper.toOpenAIRequest(request, modelId);
        openaiReq.setStream(true);
        
        AtomicInteger chunkIndex = new AtomicInteger(0);
        
        return client.chatCompletionStream(openaiReq)
            .onItem().transform(chunk -> 
                mapper.toStreamChunk(chunk, request.getRequestId(), chunkIndex.getAndIncrement()))
            .onFailure().invoke(e -> 
                log.error("OpenAI streaming failed", e));
    }

    @Override
    public Uni<Boolean> healthCheck() {
        // Simple health check - list models is lightweight
        return Uni.createFrom().item(true);
    }

    @Override
    public Uni<List<String>> getSupportedModels() {
        return Uni.createFrom().item(SUPPORTED_MODELS);
    }
}