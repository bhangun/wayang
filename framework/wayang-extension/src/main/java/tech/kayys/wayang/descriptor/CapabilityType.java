package tech.kayys.wayang.descriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Types of capabilities.
 */
public enum CapabilityType {
    // Model capabilities
    LLM,
    VISION,
    AUDIO,
    EMBEDDING,
    MULTIMODAL,
    
    // Execution capabilities
    STREAMING,
    BATCH,
    ASYNC,
    PARALLEL,
    DISTRIBUTED,
    
    // Tool capabilities
    FUNCTION_CALLING,
    TOOL_USE,
    API_CALLING,
    
    // Memory capabilities
    SHORT_TERM,
    LONG_TERM,
    VECTOR_SEARCH,
    GRAPH_SEARCH,
    
    // Reasoning capabilities
    PLANNING,
    REASONING,
    REFLECTION,
    DEBATE,
    
    // Security capabilities
    AUTHENTICATION,
    AUTHORIZATION,
    ENCRYPTION,
    AUDIT,
    
    // Integration capabilities
    EVENT_DRIVEN,
    WEBHOOK,
    MESSAGING,
    WORKFLOW,
    
    CUSTOM
}
