package tech.kayys.wayang.rag.spi.plugin;

import tech.kayys.wayang.rag.model.RagResult;
import tech.kayys.wayang.rag.model.RagScoredChunk;

import java.util.List;

/**
 * Interface for RAG pipeline plugins.
 */
public interface RagPipelinePlugin {

    String id();

    default int order() {
        return 1000;
    }

    default boolean supportsTenant(String tenantId) {
        return true;
    }

    default RagPluginExecutionContext beforeQuery(RagPluginExecutionContext context) {
        return context;
    }

    default List<RagScoredChunk> afterRetrieve(RagPluginExecutionContext context, List<RagScoredChunk> chunks) {
        return chunks;
    }

    default RagResult afterResult(RagPluginExecutionContext context, RagResult result) {
        return result;
    }
}
