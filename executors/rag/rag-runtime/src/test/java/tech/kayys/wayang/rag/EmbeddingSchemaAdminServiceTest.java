package tech.kayys.gamelan.executor.rag.langchain;

import com.fasterxml.jackson.databind.ObjectMapper;

import tech.kayys.wayang.rag.embedding.RagEmbeddingStoreFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingSchemaAdminServiceTest {

    @Mock
    private RagEmbeddingStoreFactory storeFactory;

    private EmbeddingSchemaAdminService service;

    @BeforeEach
    void setUp() {
        service = new EmbeddingSchemaAdminService();
        service.storeFactory = storeFactory;
        service.config = new RagRuntimeConfig();
        service.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void shouldReturnTenantContractStatus() {
        EmbeddingSchemaContract contract = new EmbeddingSchemaContract("hash-384", 384, "v2");
        when(storeFactory.contractForTenant("tenant-a")).thenReturn(contract);

        EmbeddingSchemaContract status = service.status("tenant-a");

        assertEquals("hash-384", status.model());
        assertEquals(384, status.dimension());
        assertEquals("v2", status.version());
    }

    @Test
    void shouldResolveDimensionFromModelAndMigrate() {
        EmbeddingSchemaContract previous = new EmbeddingSchemaContract("hash-384", 384, "v1");
        when(storeFactory.contractForTenant("tenant-a")).thenReturn(previous);

        EmbeddingSchemaMigrationRequest request = new EmbeddingSchemaMigrationRequest(
                "tenant-a",
                "tfidf-512",
                null,
                "v3",
                true,
                false);

        EmbeddingSchemaMigrationStatus result = service.migrate(request);

        assertEquals("tenant-a", result.tenantId());
        assertEquals("hash-384", result.previous().model());
        assertEquals("tfidf-512", result.current().model());
        assertEquals(512, result.current().dimension());
        assertEquals("v3", result.current().version());
        assertEquals(false, result.dryRun());
        verify(storeFactory).migrateTenantContract(
                eq("tenant-a"),
                eq("tfidf-512"),
                eq(512),
                eq("v3"),
                eq(true));
        assertEquals(1, service.history("tenant-a", 10).size());
    }

    @Test
    void shouldRejectContractChangeWithoutNamespaceClear() {
        EmbeddingSchemaContract previous = new EmbeddingSchemaContract("hash-384", 384, "v1");
        when(storeFactory.contractForTenant("tenant-a")).thenReturn(previous);

        EmbeddingSchemaMigrationRequest request = new EmbeddingSchemaMigrationRequest(
                "tenant-a",
                "hash-768",
                768,
                "v2",
                false,
                false);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.migrate(request));
        assertEquals("clearNamespace must be true when embedding contract changes", error.getMessage());
    }

    @Test
    void shouldSupportDryRunWithoutApplyingMigration() {
        EmbeddingSchemaContract previous = new EmbeddingSchemaContract("hash-384", 384, "v1");
        when(storeFactory.contractForTenant("tenant-b")).thenReturn(previous);

        EmbeddingSchemaMigrationRequest request = new EmbeddingSchemaMigrationRequest(
                "tenant-b",
                "chargram-256",
                null,
                "v7",
                true,
                true);

        EmbeddingSchemaMigrationStatus result = service.migrate(request);

        assertEquals(true, result.dryRun());
        assertEquals("chargram-256", result.current().model());
        assertEquals(256, result.current().dimension());
        verify(storeFactory, never()).migrateTenantContract(
                eq("tenant-b"),
                eq("chargram-256"),
                eq(256),
                eq("v7"),
                eq(true));
        assertEquals(1, service.history("tenant-b", 10).size());
    }

    @Test
    void shouldPersistAndReloadHistoryFromFile() throws Exception {
        Path historyFile = Files.createTempFile("embedding-schema-history", ".ndjson");

        RagRuntimeConfig runtimeConfig = new RagRuntimeConfig();
        runtimeConfig.setEmbeddingSchemaHistoryPath(historyFile.toString());
        service.config = runtimeConfig;

        EmbeddingSchemaContract previous = new EmbeddingSchemaContract("hash-384", 384, "v1");
        when(storeFactory.contractForTenant("tenant-p")).thenReturn(previous);

        service.migrate(new EmbeddingSchemaMigrationRequest(
                "tenant-p",
                "tfidf-512",
                null,
                "v2",
                true,
                true));
        assertEquals(1, service.history("tenant-p", 10).size());

        EmbeddingSchemaAdminService reloaded = new EmbeddingSchemaAdminService();
        reloaded.storeFactory = storeFactory;
        reloaded.config = runtimeConfig;
        reloaded.objectMapper = new ObjectMapper().findAndRegisterModules();

        assertEquals(1, reloaded.history("tenant-p", 10).size());
        assertEquals("tfidf-512", reloaded.history("tenant-p", 10).get(0).current().model());
    }

    @Test
    void shouldCompactHistoryByMaxEventsAndPersistRewrite() throws Exception {
        Path historyFile = Files.createTempFile("embedding-schema-compact", ".ndjson");

        RagRuntimeConfig runtimeConfig = new RagRuntimeConfig();
        runtimeConfig.setEmbeddingSchemaHistoryPath(historyFile.toString());
        service.config = runtimeConfig;

        EmbeddingSchemaContract previous = new EmbeddingSchemaContract("hash-384", 384, "v1");
        when(storeFactory.contractForTenant("tenant-c")).thenReturn(previous);

        service.migrate(new EmbeddingSchemaMigrationRequest("tenant-c", "hash-384", 384, "v1", true, true));
        service.migrate(new EmbeddingSchemaMigrationRequest("tenant-c", "hash-384", 384, "v2", true, true));
        service.migrate(new EmbeddingSchemaMigrationRequest("tenant-c", "hash-384", 384, "v3", true, true));
        assertEquals(3, service.history("tenant-c", 10).size());

        EmbeddingSchemaHistoryCompactionStatus compacted = service.compactHistory(
                "tenant-c",
                new EmbeddingSchemaHistoryCompactionRequest(2, null, false));
        assertEquals(3, compacted.beforeCount());
        assertEquals(2, compacted.afterCount());
        assertEquals(1, compacted.removedCount());

        EmbeddingSchemaAdminService reloaded = new EmbeddingSchemaAdminService();
        reloaded.storeFactory = storeFactory;
        reloaded.config = runtimeConfig;
        reloaded.objectMapper = new ObjectMapper().findAndRegisterModules();

        assertEquals(2, reloaded.history("tenant-c", 10).size());
        assertEquals("v2", reloaded.history("tenant-c", 10).get(0).current().version());
        assertEquals("v3", reloaded.history("tenant-c", 10).get(1).current().version());
    }

    @Test
    void shouldSupportCompactionDryRunWithoutMutation() {
        EmbeddingSchemaContract previous = new EmbeddingSchemaContract("hash-384", 384, "v1");
        when(storeFactory.contractForTenant("tenant-d")).thenReturn(previous);

        service.migrate(new EmbeddingSchemaMigrationRequest("tenant-d", "hash-384", 384, "v1", true, true));
        service.migrate(new EmbeddingSchemaMigrationRequest("tenant-d", "hash-384", 384, "v2", true, true));
        service.migrate(new EmbeddingSchemaMigrationRequest("tenant-d", "hash-384", 384, "v3", true, true));
        assertEquals(3, service.history("tenant-d", 10).size());

        EmbeddingSchemaHistoryCompactionStatus dryRun = service.compactHistory(
                "tenant-d",
                new EmbeddingSchemaHistoryCompactionRequest(1, null, true));
        assertEquals(true, dryRun.dryRun());
        assertEquals(3, dryRun.beforeCount());
        assertEquals(1, dryRun.afterCount());
        assertEquals(3, service.history("tenant-d", 10).size());
    }
}
