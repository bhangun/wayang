package tech.kayys.wayang.models.api.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;

/**
 * Primary service interface for model inference.
 * Provides unified API for all model types and providers.
 */
public interface ModelService {
    
    /**
     * Execute synchronous model inference.
     * 
     * @param request Model request
     * @return Completed model response
     */
    Uni<ModelResponse> infer(ModelRequest request);
    
    /**
     * Execute streaming model inference.
     * 
     * @param request Model request with stream=true
     * @return Stream of response chunks
     */
    Multi<StreamChunk> inferStream(ModelRequest request);
    
    /**
     * Check health of model service.
     * 
     * @return Health status
     */
    Uni<Boolean> healthCheck();
}