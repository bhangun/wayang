package tech.kayys.wayang.rag;

import java.util.List;
import java.util.Map;

import tech.kayys.wayang.rag.GenerationConfig;
import tech.kayys.wayang.rag.RagMode;
import tech.kayys.wayang.rag.RetrievalConfig;
import tech.kayys.wayang.rag.SearchStrategy;

public record RagQueryRequest(
                String tenantId,
                String query,
                RagMode ragMode,
                SearchStrategy searchStrategy,
                RetrievalConfig retrievalConfig,
                GenerationConfig generationConfig,
                List<String> collections,
                Map<String, Object> filters) {
}