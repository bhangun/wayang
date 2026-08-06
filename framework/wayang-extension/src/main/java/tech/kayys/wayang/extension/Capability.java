package tech.kayys.wayang.extension;
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
 * Capabilities that extensions can support.
 */
public enum Capability {
    // Model capabilities
    LLM,
    VISION,
    AUDIO,
    EMBEDDING,
    MULTIMODAL,
    TEXT_GENERATION,
    IMAGE_GENERATION,
    AUDIO_GENERATION,
    
    // Execution capabilities
    STREAMING,
    BATCH,
    ASYNC,
    PARALLEL,
    DISTRIBUTED,
    EVENT_DRIVEN,
    
    // Tool capabilities
    FUNCTION_CALLING,
    TOOL_USE,
    API_CALLING,
    SHELL_EXECUTION,
    
    // Memory capabilities
    SHORT_TERM_MEMORY,
    LONG_TERM_MEMORY,
    VECTOR_SEARCH,
    GRAPH_SEARCH,
    SEMANTIC_SEARCH,
    
    // Reasoning capabilities
    PLANNING,
    REASONING,
    REFLECTION,
    DEBATE,
    CHAIN_OF_THOUGHT,
    TREE_OF_THOUGHTS,
    
    // Security capabilities
    AUTHENTICATION,
    AUTHORIZATION,
    ENCRYPTION,
    AUDIT,
    
    // Integration capabilities
    WEBHOOK,
    MESSAGING,
    WORKFLOW,
    EVENT_BUS,
    
    // Data capabilities
    VECTOR_STORE,
    DOCUMENT_STORE,
    KNOWLEDGE_GRAPH,
    CACHE,
    
    // Observability
    TELEMETRY,
    LOGGING,
    METRICS,
    TRACING,
    
    CUSTOM
}
