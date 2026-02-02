
/**
 * KEYWORD RETRIEVAL STRATEGY - FULL BM25 IMPLEMENTATION
 */
public class KeywordRetrievalStrategy implements RetrievalStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(KeywordRetrievalStrategy.class);

    @Override
    public List<ScoredDocument> retrieve(
            String query,
            EmbeddingStore<TextSegment> store,
            RetrievalConfig config) {

        LOG.debug("Keyword retrieval (BM25) for query: {}", query);

        // For in-memory stores, we can iterate; for production use dedicated keyword
        // index
        if (!(store instanceof InMemoryEmbeddingStore)) {
            LOG.warn("Keyword search not optimized for store type: {}", store.getClass());
            return List.of();
        }

        try {
            // Get all documents from store
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(new Embedding(new float[1536])) // Dummy embedding
                    .maxResults(10000)
                    .minScore(0.0)
                    .build();

            List<TextSegment> allDocs = store.search(request).matches().stream()
                    .map(EmbeddingMatch::embedded)
                    .collect(Collectors.toList());

            // Calculate BM25 scores
            return calculateBM25Scores(query, allDocs, config.topK());

        } catch (Exception e) {
            LOG.error("Keyword retrieval failed", e);
            return List.of();
        }
    }

    private List<ScoredDocument> calculateBM25Scores(
            String query,
            List<TextSegment> documents,
            int topK) {

        double k1 = 1.5;
        double b = 0.75;

        // Tokenize query
        List<String> queryTerms = tokenize(query);

        // Calculate document stats
        int totalDocs = documents.size();
        double avgDocLength = documents.stream()
                .mapToInt(doc -> tokenize(doc.text()).size())
                .average()
                .orElse(0.0);

        // Calculate term frequencies
        Map<String, Integer> docFreqs = new HashMap<>();
        for (TextSegment doc : documents) {
            Set<String> uniqueTerms = new HashSet<>(tokenize(doc.text()));
            for (String term : uniqueTerms) {
                docFreqs.merge(term, 1, Integer::sum);
            }
        }

        // Calculate BM25 score for each document
        List<ScoredDocument> scoredDocs = new ArrayList<>();

        for (TextSegment doc : documents) {
            List<String> docTerms = tokenize(doc.text());
            Map<String, Integer> termFreq = new HashMap<>();
            for (String term : docTerms) {
                termFreq.merge(term, 1, Integer::sum);
            }

            double score = 0.0;
            for (String term : queryTerms) {
                int df = docFreqs.getOrDefault(term, 0);
                if (df == 0)
                    continue;

                double idf = Math.log((totalDocs - df + 0.5) / (df + 0.5) + 1.0);
                int tf = termFreq.getOrDefault(term, 0);

                score += idf * (tf * (k1 + 1)) /
                        (tf + k1 * (1 - b + b * docTerms.size() / avgDocLength));
            }

            if (score > 0) {
                scoredDocs.add(new ScoredDocument(doc, score));
            }
        }

        // Sort and return top K
        scoredDocs.sort(Comparator.comparingDouble(ScoredDocument::score).reversed());
        return scoredDocs.stream().limit(topK).collect(Collectors.toList());
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+"))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toList());
    }
}