package tech.kayys.gamelan.executor.rag.langchain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.wayang.embedding.EmbeddingConfigRuntime;
import tech.kayys.wayang.embedding.EmbeddingModuleConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingConfigAdminServiceTest {

    @Mock
    private EmbeddingConfigRuntime runtime;

    private EmbeddingConfigAdminService service;

    @BeforeEach
    void setUp() {
        service = new EmbeddingConfigAdminService();
        service.runtime = runtime;
    }

    @Test
    void shouldReturnCurrentStatus() {
        EmbeddingModuleConfig config = new EmbeddingModuleConfig();
        config.setDefaultProvider("tfidf");
        config.setDefaultModel("tfidf-512");
        config.setEmbeddingVersion("v9");
        config.setNormalize(false);
        config.setTenantStrategy("tenant-a", "hash", "hash-384");
        when(runtime.current()).thenReturn(config);

        EmbeddingConfigStatus status = service.status();

        assertEquals("tfidf", status.defaultProvider());
        assertEquals("tfidf-512", status.defaultModel());
        assertEquals("v9", status.embeddingVersion());
        assertEquals(false, status.normalize());
        assertEquals("hash", status.tenantStrategies().get("tenant-a").provider());
        assertEquals("hash-384", status.tenantStrategies().get("tenant-a").model());
    }

    @Test
    void shouldReloadAndReturnStatus() {
        EmbeddingModuleConfig config = new EmbeddingModuleConfig();
        config.setDefaultProvider("hash");
        config.setDefaultModel("hash-768");
        config.setEmbeddingVersion("v2");
        when(runtime.current()).thenReturn(config);

        EmbeddingConfigStatus status = service.reload();

        verify(runtime).reload();
        assertEquals("hash", status.defaultProvider());
        assertEquals("hash-768", status.defaultModel());
        assertEquals("v2", status.embeddingVersion());
    }
}
