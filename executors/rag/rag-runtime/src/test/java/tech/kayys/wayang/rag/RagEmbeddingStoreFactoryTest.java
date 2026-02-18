package tech.kayys.gamelan.executor.rag.langchain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gamelan.executor.rag.domain.RetrievalConfig;
import tech.kayys.wayang.rag.core.model.RagChunk;
import tech.kayys.wayang.rag.core.store.InMemoryVectorStore;
import tech.kayys.wayang.rag.core.store.VectorStore;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagEmbeddingStoreFactoryTest {

    @Mock
    private RagVectorStoreProvider ragVectorStoreProvider;

    @Mock
    private VectorStore<RagChunk> vectorStore;

    private RagEmbeddingStoreFactory storeFactory;

    @BeforeEach
    void setUp() {
        storeFactory = new RagEmbeddingStoreFactory();
        storeFactory.ragVectorStoreProvider = ragVectorStoreProvider;
        storeFactory.config = new RagRuntimeConfig();
    }

    @Test
    void testGetStore() {
        InMemoryVectorStore<RagChunk> backingStore = new InMemoryVectorStore<>();
        when(ragVectorStoreProvider.getStore()).thenReturn(backingStore);

        RagRuntimeConfig runtimeConfig = new RagRuntimeConfig();
        runtimeConfig.setEmbeddingModel("hash-8");
        runtimeConfig.setEmbeddingDimension(8);
        runtimeConfig.setEmbeddingVersion("v3");
        storeFactory.config = runtimeConfig;

        RagEmbeddingStore result = storeFactory.getStore("test-tenant", RetrievalConfig.defaults());

        assertNotNull(result);
        String id = result.add(new float[8], "hello", Map.of("kind", "doc"));
        var hits = result.search(new float[8], 5, -1.0, Map.of("kind", "doc"));
        assertEquals(id, hits.get(0).id());
        assertEquals("v3", hits.get(0).metadata().get("embeddingVersion"));
    }

    @Test
    void shouldMigrateTenantContractAndClearNamespace() {
        InMemoryVectorStore<RagChunk> backingStore = new InMemoryVectorStore<>();
        when(ragVectorStoreProvider.getStore()).thenReturn(backingStore);

        RagRuntimeConfig runtimeConfig = new RagRuntimeConfig();
        runtimeConfig.setEmbeddingModel("hash-8");
        runtimeConfig.setEmbeddingDimension(8);
        runtimeConfig.setEmbeddingVersion("v1");
        storeFactory.config = runtimeConfig;

        RagEmbeddingStore initial = storeFactory.getStore("tenant-x", RetrievalConfig.defaults());
        initial.add(new float[8], "before", Map.of("tag", "state"));
        assertEquals(1, initial.search(new float[8], 10, -1.0, Map.of("tag", "state")).size());

        EmbeddingSchemaContract previous = storeFactory.migrateTenantContract(
                "tenant-x",
                "hash-16",
                16,
                "v2",
                true);

        assertEquals("hash-8", previous.model());
        assertEquals(8, previous.dimension());
        assertEquals("v1", previous.version());
        assertEquals("v2", storeFactory.contractForTenant("tenant-x").version());

        RagEmbeddingStore migrated = storeFactory.getStore("tenant-x", RetrievalConfig.defaults());
        migrated.add(new float[16], "after", Map.of("tag", "state"));
        assertEquals(1, migrated.search(new float[16], 10, -1.0, Map.of("tag", "state")).size());
    }
}
