package tech.kayys.wayang.models.api.provider;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;

/**
 * Provider adapter interface.
 * Each provider (Ollama, OpenAI, vLLM, etc.) implements this.
 */
public interface ModelProvider {
    
    /**
     * Get provider name.
     * 
     * @return Provider identifier (e.g., "ollama", "openai")
     */
    String getProviderName();
    
    /**
     * Execute inference request.
     * 
     * @param request Model request
     * @param modelId Resolved model ID for this provider
     * @return Model response
     */
    Uni<ModelResponse> infer(ModelRequest request, String modelId);
    
    /**
     * Execute streaming inference.
     * 
     * @param request Model request
     * @param modelId Resolved model ID
     * @return Stream of chunks
     */
    Multi<StreamChunk> inferStream(ModelRequest request, String modelId);
    
    /**
     * Check provider health.
     * 
     * @return Health status
     */
    Uni<Boolean> healthCheck();
    
    /**
     * Get supported model IDs for this provider.
     * 
     * @return List of model IDs
     */
    Uni<java.util.List<String>> getSupportedModels();
}