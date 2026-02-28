package tech.kayys.wayang.rag;


import java.util.List;
import java.util.Map;

import tech.kayys.wayang.rag.domain.GenerationConfig;
import tech.kayys.wayang.rag.domain.RagMode;
import tech.kayys.wayang.rag.domain.RetrievalConfig;
import tech.kayys.wayang.rag.domain.SearchStrategy;

record RagQueryRequest(
        String tenantId,
        String query,
        RagMode ragMode,
        SearchStrategy searchStrategy,
        RetrievalConfig retrievalConfig,
        GenerationConfig generationConfig,
        List<String> collections,
        Map<String, Object> filters) {
}