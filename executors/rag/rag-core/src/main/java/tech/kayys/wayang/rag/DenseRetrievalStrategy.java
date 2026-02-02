
/**
 * DENSE RETRIEVAL STRATEGY - FULL IMPLEMENTATION
 */
public class DenseRetrievalStrategy implements RetrievalStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(DenseRetrievalStrategy.class);
    private final EmbeddingModelFactory modelFactory;

    DenseRetrievalStrategy(EmbeddingModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    @Override
    public List<ScoredDocument> retrieve(
            String query,
            EmbeddingStore<TextSegment> store,
            RetrievalConfig config) {

        LOG.debug("Dense retrieval for query: {}", query);

        try {
            // Create embedding model
            EmbeddingModel embeddingModel = modelFactory.createModel(
                    "openai",
                    "text-embedding-3-small",
                    System.getenv("OPENAI_API_KEY"));

            // Generate query embedding
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // Search in embedding store
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(config.topK())
                    .minScore(config.minScore())
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = store.search(searchRequest);

            // Convert to scored documents
            return searchResult.matches().stream()
                    .map(match -> new ScoredDocument(match.embedded(), match.score()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            LOG.error("Dense retrieval failed", e);
            return List.of();
        }
    }
}