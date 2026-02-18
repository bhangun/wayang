package tech.kayys.gamelan.executor.rag.langchain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.executor.rag.domain.ChunkingConfig;
import tech.kayys.gamelan.executor.rag.domain.GenerationConfig;
import tech.kayys.gamelan.executor.rag.domain.RetrievalConfig;
import tech.kayys.wayang.embedding.EmbeddingService;
import tech.kayys.wayang.rag.core.impl.RagIndexer;
import tech.kayys.wayang.rag.core.impl.RagPipeline;
import tech.kayys.wayang.rag.core.impl.SimpleTextDocumentParser;
import tech.kayys.wayang.rag.core.impl.SlidingWindowChunker;
import tech.kayys.wayang.rag.core.impl.TopKReranker;
import tech.kayys.wayang.rag.core.impl.VectorRetriever;
import tech.kayys.wayang.rag.core.model.RagChunk;
import tech.kayys.wayang.rag.core.model.RagQuery;
import tech.kayys.wayang.rag.core.model.RagResult;
import tech.kayys.wayang.rag.core.model.RagScoredChunk;
import tech.kayys.wayang.rag.core.spi.ChunkingOptions;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class NativeRagCoreService {

    @Inject
    EmbeddingService embeddingService;

    @Inject
    RagRuntimeConfig config;

    @Inject
    RagVectorStoreProvider vectorStoreProvider;

    @Inject
    NativeGenerationService nativeGenerationService;

    @Inject
    RagPluginManager pluginManager;

    public List<RagChunk> ingestText(
            String tenantId,
            String source,
            String content,
            Map<String, Object> metadata,
            ChunkingConfig chunkingConfig) {

        ChunkingOptions options = new ChunkingOptions(
                chunkingConfig.chunkSize(),
                chunkingConfig.chunkOverlap());
        return createPipeline(tenantId, GenerationConfig.defaults()).ingest(source, content, metadata, options);
    }

    public RagResult query(
            String tenantId,
            String query,
            RetrievalConfig retrievalConfig,
            GenerationConfig generationConfig,
            Map<String, Object> filters) {

        RagPluginExecutionContext pluginContext = pluginContext(
                tenantId,
                query,
                retrievalConfig,
                generationConfig,
                filters,
                false);
        if (pluginManager != null) {
            pluginContext = pluginManager.applyBeforeQuery(pluginContext);
        }

        RagQuery ragQuery = new RagQuery(
                pluginContext.query(),
                pluginContext.topK(),
                pluginContext.minSimilarity(),
                pluginContext.filters());
        RagResult result = createPipeline(tenantId, generationConfig).query(ragQuery);
        if (pluginManager == null) {
            return result;
        }
        java.util.List<tech.kayys.wayang.rag.core.model.RagScoredChunk> transformedChunks = pluginManager.applyAfterRetrieve(
                pluginContext,
                result.chunks());
        RagResult transformed = new RagResult(
                result.query(),
                transformedChunks,
                result.answer(),
                result.metadata());
        return pluginManager.applyAfterResult(pluginContext, transformed);
    }

    public List<RagScoredChunk> retrieve(
            String tenantId,
            String query,
            RetrievalConfig retrievalConfig,
            Map<String, Object> filters) {

        RagPluginExecutionContext pluginContext = pluginContext(
                tenantId,
                query,
                retrievalConfig,
                GenerationConfig.defaults(),
                filters,
                true);
        if (pluginManager != null) {
            pluginContext = pluginManager.applyBeforeQuery(pluginContext);
        }

        RagQuery ragQuery = new RagQuery(
                pluginContext.query(),
                pluginContext.topK(),
                pluginContext.minSimilarity(),
                pluginContext.filters());
        List<RagScoredChunk> chunks = createRetrievalOnlyPipeline(tenantId).query(ragQuery).chunks();
        if (pluginManager == null) {
            return chunks;
        }
        return pluginManager.applyAfterRetrieve(pluginContext, chunks);
    }

    private static RagPluginExecutionContext pluginContext(
            String tenantId,
            String query,
            RetrievalConfig retrievalConfig,
            GenerationConfig generationConfig,
            Map<String, Object> filters,
            boolean retrievalOnly) {
        return new RagPluginExecutionContext(
                tenantId,
                query,
                retrievalConfig.topK(),
                retrievalConfig.minSimilarity(),
                filters == null ? Map.of() : filters,
                generationConfig,
                retrievalOnly);
    }

    private RagPipeline createPipeline(String tenantId, GenerationConfig generationConfig) {
        RagIndexer indexer = new RagIndexer(
                embeddingService,
                vectorStoreProvider.getStore(),
                tenantId,
                config.getEmbeddingModel());

        VectorRetriever retriever = new VectorRetriever(
                embeddingService,
                vectorStoreProvider.getStore(),
                tenantId,
                config.getEmbeddingModel());

        return new RagPipeline(
                new SimpleTextDocumentParser(),
                new SlidingWindowChunker(),
                indexer,
                retriever,
                new TopKReranker(),
                (query, context) -> nativeGenerationService.generate(query, context, generationConfig));
    }

    private RagPipeline createRetrievalOnlyPipeline(String tenantId) {
        RagIndexer indexer = new RagIndexer(
                embeddingService,
                vectorStoreProvider.getStore(),
                tenantId,
                config.getEmbeddingModel());

        VectorRetriever retriever = new VectorRetriever(
                embeddingService,
                vectorStoreProvider.getStore(),
                tenantId,
                config.getEmbeddingModel());

        return new RagPipeline(
                new SimpleTextDocumentParser(),
                new SlidingWindowChunker(),
                indexer,
                retriever,
                new TopKReranker(),
                (query, context) -> "");
    }
}
