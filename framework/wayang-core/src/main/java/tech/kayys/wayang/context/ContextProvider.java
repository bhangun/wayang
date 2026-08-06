package tech.kayys.wayang.context;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Set;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.extension.Extension;

/**
 * Context Provider - loads contextual data.
 */
public interface ContextProvider extends Extension {
    
    /**
     * Load context data
     */
    ContextData load(AgentContext context) throws Exception;
    
    /**
     * Load context data with a specific query
     */
    default ContextData loadWithQuery(AgentContext context, String query) throws Exception {
        return load(context);
    }
    
    /**
     * Load context data asynchronously
     */
    default CompletableFuture<ContextData> loadAsync(AgentContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return load(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get supported context types
     */
    default Set<ContextType> getSupportedTypes() {
        return Set.of(ContextType.VECTOR_RAG);
    }
    
    /**
     * Check if context is available
     */
    default boolean isAvailable(AgentContext context) {
        return true;
    }
}