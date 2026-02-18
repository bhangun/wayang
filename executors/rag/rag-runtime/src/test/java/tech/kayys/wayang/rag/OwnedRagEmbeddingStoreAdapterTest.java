package tech.kayys.gamelan.executor.rag.langchain;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.rag.core.model.RagChunk;
import tech.kayys.wayang.rag.core.store.InMemoryVectorStore;
import tech.kayys.wayang.rag.core.store.VectorStore;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnedRagEmbeddingStoreAdapterTest {

    @Test
    void shouldEnforceDimensionAndMetadataContract() {
        VectorStore<RagChunk> store = new InMemoryVectorStore<>();
        OwnedRagEmbeddingStoreAdapter adapter = new OwnedRagEmbeddingStoreAdapter(
                "tenant-a",
                "hash-384",
                384,
                store);

        String id = adapter.add(new float[384], "hello", Map.of("collection", "docs"));
        var hits = adapter.search(new float[384], 5, -1.0, Map.of("collection", "docs"));

        assertEquals(1, hits.size());
        assertEquals(id, hits.get(0).id());
        assertEquals("tenant-a", hits.get(0).metadata().get("tenantId"));
        assertEquals("hash-384", hits.get(0).metadata().get("embeddingModel"));
        assertEquals(384, hits.get(0).metadata().get("embeddingDimension"));
        assertEquals("v1", hits.get(0).metadata().get("embeddingVersion"));
    }

    @Test
    void shouldFailOnTenantMismatchInMetadata() {
        VectorStore<RagChunk> store = new InMemoryVectorStore<>();
        OwnedRagEmbeddingStoreAdapter adapter = new OwnedRagEmbeddingStoreAdapter(
                "tenant-a",
                "hash-384",
                384,
                store);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.add(new float[384], "hello", Map.of("tenantId", "tenant-b")));
        assertTrue(error.getMessage().contains("tenantId mismatch"));
    }

    @Test
    void shouldFailOnDimensionMismatch() {
        VectorStore<RagChunk> store = new InMemoryVectorStore<>();
        OwnedRagEmbeddingStoreAdapter adapter = new OwnedRagEmbeddingStoreAdapter(
                "tenant-a",
                "hash-384",
                384,
                store);

        assertThrows(IllegalArgumentException.class, () -> adapter.add(new float[128], "hello", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> adapter.search(new float[128], 5, 0.0, Map.of()));
    }
}
