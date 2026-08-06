package tech.kayys.wayang.knowledge;
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

import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.extension.Extension;

/**
 * Knowledge Provider - retrieves knowledge.
 */
public interface KnowledgeProvider extends Extension {
    
    /**
     * Retrieve knowledge
     */
    KnowledgeResult retrieve(KnowledgeRequest request) throws Exception;
    
    /**
     * Retrieve with execution context
     */
    default KnowledgeResult retrieve(KnowledgeRequest request, ExecutionContext context) throws Exception {
        return retrieve(request);
    }
    
    /**
     * Retrieve asynchronously
     */
    default CompletableFuture<KnowledgeResult> retrieveAsync(KnowledgeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return retrieve(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get knowledge source info
     */
    default KnowledgeSourceInfo getSourceInfo() {
        return new KnowledgeSourceInfo("default", "unknown", Set.of());
    }
}
