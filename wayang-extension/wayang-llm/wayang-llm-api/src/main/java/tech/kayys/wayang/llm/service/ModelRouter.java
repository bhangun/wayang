package tech.kayys.wayang.models.api.service;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;

import java.util.List;

/**
 * Model routing service - selects best model based on policies.
 */
public interface ModelRouter {
    
    /**
     * Select best model for request based on routing policy.
     * 
     * @param request Model request with hints
     * @return Selected model metadata
     */
    Uni<ModelMetadata> selectModel(ModelRequest request);
    
    /**
     * Get candidate models ranked by suitability.
     * 
     * @param request Model request
     * @return Ranked list of candidate models
     */
    Uni<List<ModelMetadata>> getCandidates(ModelRequest request);
    
    /**
     * Select fallback model if primary fails.
     * 
     * @param request Original request
     * @param failedModelId Failed model ID
     * @return Fallback model metadata
     */
    Uni<ModelMetadata> selectFallback(ModelRequest request, String failedModelId);
}