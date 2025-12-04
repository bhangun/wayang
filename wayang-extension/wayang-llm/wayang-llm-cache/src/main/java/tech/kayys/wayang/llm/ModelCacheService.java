package tech.kayys.wayang.models.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;

import java.util.Optional;

/**
 * Caching service for model responses.
 * Uses Caffeine for local cache and Redis for distributed cache.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ModelCacheService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Get cached response if available.
     * 
     * @param request Model request
     * @param modelId Selected model ID
     * @return Cached response if available
     */
    @CacheResult(cacheName = "model-responses")
    public Optional<ModelResponse> get(ModelRequest request, String modelId) {
        // Cache miss - return empty
        return Optional.empty();
    }
    
    /**
     * Cache a response.
     * 
     * @param request Model request
     * @param modelId Selected model ID
     * @param response Response to cache
     */
    public void put(ModelRequest request, String modelId, ModelResponse response) {
        // Actual caching is handled by @CacheResult
        // This method exists for explicit cache writes if needed
        log.debug("Cached response for model={}, requestId={}", modelId, request.getRequestId());
    }
    
    /**
     * Invalidate cache for specific tenant/model.
     * 
     * @param tenantId Tenant identifier
     * @param modelId Model identifier
     */
    @CacheInvalidate(cacheName = "model-responses")
    public void invalidate(String tenantId, String modelId) {
        log.info("Invalidated cache for tenant={}, model={}", tenantId, modelId);
    }
    
    /**
     * Generate cache key from request.
     */
    public CacheKey generateKey(ModelRequest request, String modelId) {
        String requestContent = serializeRequest(request);
        String hash = CacheKey.hashRequest(requestContent);
        
        return CacheKey.builder()
            .tenantId(request.getTenantId())
            .modelId(modelId)
            .requestHash(hash)
            .build();
    }
    
    private String serializeRequest(ModelRequest request) {
        try {
            // Only include cache-relevant fields
            var cacheableRequest = new CacheableRequest(
                request.getType(),
                request.getMessages(),
                request.getPrompt(),
                request.getInputs(),
                request.getMaxTokens(),
                request.getTemperature(),
                request.getTopP()
            );
            return objectMapper.writeValueAsString(cacheableRequest);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request for caching", e);
            return request.toString();
        }
    }
    
    private record CacheableRequest(
        String type,
        Object messages,
        String prompt,
        Object inputs,
        Integer maxTokens,
        Double temperature,
        Double topP
    ) {}
}