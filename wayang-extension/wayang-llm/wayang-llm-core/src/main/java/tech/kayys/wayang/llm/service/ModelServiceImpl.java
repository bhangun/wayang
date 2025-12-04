package tech.kayys.wayang.models.core.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;
import tech.kayys.wayang.models.api.exception.ModelException;
import tech.kayys.wayang.models.api.exception.ProviderUnavailableException;
import tech.kayys.wayang.models.api.provider.ModelProvider;
import tech.kayys.wayang.models.api.service.ModelRouter;
import tech.kayys.wayang.models.api.service.ModelService;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main implementation of ModelService.
 * Orchestrates routing, provider selection, and execution.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ModelServiceImpl implements ModelService {
    
    private final ModelRouter modelRouter;
    private final Instance<ModelProvider> providers;
    private final Map<String, ModelProvider> providerCache = new ConcurrentHashMap<>();

    @Override
    public Uni<ModelResponse> infer(ModelRequest request) {
        log.info("Processing inference request: {}", request.getRequestId());
        
        return modelRouter.selectModel(request)
            .onItem().transformToUni(model -> {
                ModelProvider provider = getProvider(model.getProvider());
                
                return provider.infer(request, model.getModelId())
                    .ifNoItem().after(Duration.ofMillis(request.getTimeoutMs()))
                    .failWith(() -> new ModelException("TIMEOUT",
                        "Request timeout after " + request.getTimeoutMs() + "ms"))
                    .onFailure().retry().atMost(2)
                    .onFailure().recoverWithUni(failure -> 
                        handleInferenceFailure(request, model, failure));
            });
    }

    @Override
    public Multi<StreamChunk> inferStream(ModelRequest request) {
        log.info("Processing streaming inference request: {}", request.getRequestId());
        
        return Multi.createFrom().uni(
            modelRouter.selectModel(request)
                .onItem().transform(model -> {
                    ModelProvider provider = getProvider(model.getProvider());
                    return Map.entry(provider, model.getModelId());
                })
        ).onItem().transformToMultiAndConcatenate(entry -> 
            entry.getKey().inferStream(request, entry.getValue())
                .ifNoItem().after(Duration.ofMillis(request.getTimeoutMs()))
                .failWith(() -> new ModelException("TIMEOUT",
                    "Stream timeout after " + request.getTimeoutMs() + "ms"))
        );
    }

    @Override
    public Uni<Boolean> healthCheck() {
        return Uni.createFrom().item(true);
    }
    
    private ModelProvider getProvider(String providerName) {
        return providerCache.computeIfAbsent(providerName, name -> {
            for (ModelProvider provider : providers) {
                if (provider.getProviderName().equals(name)) {
                    return provider;
                }
            }
            throw new ProviderUnavailableException(name);
        });
    }
    
    private Uni<ModelResponse> handleInferenceFailure(
            ModelRequest request, 
            ModelMetadata failedModel,
            Throwable failure) {
        
        log.warn("Inference failed for model {}: {}", 
            failedModel.getModelId(), failure.getMessage());
        
        // Try fallback
        return modelRouter.selectFallback(request, failedModel.getModelId())
            .onItem().transformToUni(fallbackModel -> {
                if (fallbackModel == null) {
                    return Uni.createFrom().failure(failure);
                }
                
                log.info("Attempting fallback to model: {}", fallbackModel.getModelId());
                ModelProvider provider = getProvider(fallbackModel.getProvider());
                return provider.infer(request, fallbackModel.getModelId());
            })
            .onFailure().transform(fallbackFailure -> {
                log.error("Fallback also failed", fallbackFailure);
                return new ModelException("ALL_PROVIDERS_FAILED",
                    "Both primary and fallback models failed", failure);
            });
    }
}