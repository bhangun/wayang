package tech.kayys.wayang.inference;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.plugin.execution.ExecutionContext;




/**
 * Inference Provider - generates completions from models.
 */
public interface InferenceProvider extends Extension {
    
    /**
     * Generate a completion
     */
    CompletionResult generate(CompletionRequest request) throws Exception;
    
    /**
     * Generate with execution context
     */
    default CompletionResult generate(CompletionRequest request, ExecutionContext context) throws Exception {
        return generate(request);
    }
    
    /**
     * Stream generation
     */
    default CompletionStream stream(CompletionRequest request) throws Exception {
        throw new UnsupportedOperationException("Streaming not supported");
    }
    
    /**
     * Generate asynchronously
     */
    default CompletableFuture<CompletionResult> generateAsync(CompletionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generate(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Check if streaming is supported
     */
    default boolean supportsStreaming() {
        return false;
    }
    
    /**
     * List available models
     */
    default Set<String> listModels() {
        return Set.of();
    }
    
    /**
     * Get model info
     */
    default ModelInfo getModelInfo(String modelId) {
        return new ModelInfo(modelId, "unknown", null, Set.of(), Map.of());
    }
    
    /**
     * Validate request
     */
    default boolean validate(CompletionRequest request) {
        return request != null && request.messages() != null && !request.messages().isEmpty();
    }
}