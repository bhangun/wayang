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



/**
 * Context Types
 */
public enum ContextType {
    NONE,
    VECTOR_RAG,
    GRAPH_RAG,
    HYBRID_RAG,
    SQL,
    KNOWLEDGE_GRAPH,
    MEMORY,
    CACHE,
    CRM,
    ERP,
    EXTERNAL_API,
    FILE_SYSTEM,
    WEB_SEARCH,
    KNOWLEDGE,
    POLICY,
    EVIDENCE,
    DOCUMENT,
    STRUCTURED_DATA,
    TOOL_OUTPUT,
    USER_PROVIDED
}