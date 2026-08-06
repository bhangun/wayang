package tech.kayys.wayang.rag.runtime.store;

import tech.kayys.wayang.rag.model.RagChunk;
import tech.kayys.wayang.rag.spi.VectorSearchHit;
import tech.kayys.wayang.rag.spi.VectorStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple in-memory vector store backed by brute-force cosine similarity.
 * Suitable for tests and small corpora; swap for PgVector or Faiss in production.
 */
public class InMemoryVectorStore implements VectorStore {

    private record Entry(float[] vector, RagChunk payload, Map<String, Object> metadata) {}

    private final Map<String, Map<String, Entry>> store = new ConcurrentHashMap<>();

    @Override
    public void upsert(String ns, String id, float[] vector, RagChunk payload, Map<String, Object> metadata) {
        store.computeIfAbsent(ns, k -> new ConcurrentHashMap<>())
             .put(id, new Entry(vector, payload, metadata == null ? Map.of() : metadata));
    }

    @Override
    public List<VectorSearchHit> search(String ns, float[] query, int topK, double minScore, Map<String, Object> filters) {
        Map<String, Entry> ns_map = store.getOrDefault(ns, Map.of());
        List<VectorSearchHit> hits = new ArrayList<>();
        for (Map.Entry<String, Entry> e : ns_map.entrySet()) {
            double score = cosine(query, e.getValue().vector());
            if (score >= minScore) {
                hits.add(new VectorSearchHit(e.getValue().payload(), score, e.getKey()));
            }
        }
        hits.sort((a, b) -> Double.compare(b.score(), a.score()));
        return hits.subList(0, Math.min(topK, hits.size()));
    }

    @Override
    public boolean delete(String ns, String id) {
        Map<String, Entry> ns_map = store.get(ns);
        return ns_map != null && ns_map.remove(id) != null;
    }

    @Override
    public void clear(String ns) {
        store.remove(ns);
    }

    @Override
    public List<VectorSearchHit> keywordSearch(String ns, String query, int topK, Map<String, Object> filters) {
        String lq = query.toLowerCase();
        Map<String, Entry> ns_map = store.getOrDefault(ns, Map.of());
        List<VectorSearchHit> hits = new ArrayList<>();
        for (Map.Entry<String, Entry> e : ns_map.entrySet()) {
            RagChunk chunk = e.getValue().payload();
            if (chunk != null && chunk.text() != null && chunk.text().toLowerCase().contains(lq)) {
                hits.add(new VectorSearchHit(chunk, 1.0, e.getKey()));
            }
        }
        return hits.subList(0, Math.min(topK, hits.size()));
    }

    private static double cosine(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0 ? 0.0 : dot / denom;
    }
}
