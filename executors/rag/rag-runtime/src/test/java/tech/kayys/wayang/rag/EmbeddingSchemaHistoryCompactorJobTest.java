package tech.kayys.gamelan.executor.rag.langchain;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingSchemaHistoryCompactorJobTest {

    @Mock
    private EmbeddingSchemaAdminService schemaAdminService;

    @Mock
    private Config config;

    private SimpleMeterRegistry meterRegistry;

    private EmbeddingSchemaHistoryCompactorJob job;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        job = new EmbeddingSchemaHistoryCompactorJob();
        job.schemaAdminService = schemaAdminService;
        EmbeddingSchemaHistoryCompactorMetrics metrics = new EmbeddingSchemaHistoryCompactorMetrics(meterRegistry);
        metrics.initGauges();
        job.metrics = metrics;
    }

    @Test
    void shouldCompactAllTenantsWhenPolicyTenantsEmpty() {
        when(schemaAdminService.tenantIdsWithHistory()).thenReturn(Set.of("t1", "t2"));
        when(schemaAdminService.compactHistory(eq("t1"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new EmbeddingSchemaHistoryCompactionStatus("t1", 3, 2, 1, false, java.time.Instant.now()));
        when(schemaAdminService.compactHistory(eq("t2"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new EmbeddingSchemaHistoryCompactionStatus("t2", 5, 4, 1, false, java.time.Instant.now()));

        job.runCompactionCycle(new EmbeddingSchemaHistoryCompactorJob.CompactionPolicy(
                true,
                100,
                30,
                false,
                Set.of()));

        ArgumentCaptor<EmbeddingSchemaHistoryCompactionRequest> requestCaptor =
                ArgumentCaptor.forClass(EmbeddingSchemaHistoryCompactionRequest.class);
        verify(schemaAdminService, times(2)).compactHistory(org.mockito.ArgumentMatchers.anyString(), requestCaptor.capture());
        assertEquals(100, requestCaptor.getAllValues().get(0).maxEvents());
        assertEquals(30, requestCaptor.getAllValues().get(0).maxAgeDays());
        assertEquals(false, requestCaptor.getAllValues().get(0).dryRun());
    }

    @Test
    void shouldLoadPolicyFromConfig() {
        Map<String, Object> values = Map.of(
                "rag.runtime.embedding.schema.history.compaction.enabled", true,
                "rag.runtime.embedding.schema.history.compaction.max-events", 77,
                "rag.runtime.embedding.schema.history.compaction.max-age-days", 9,
                "rag.runtime.embedding.schema.history.compaction.dry-run", true,
                "rag.runtime.embedding.schema.history.compaction.tenants", "a,b , c");
        mockOptionalValue(config, values);

        EmbeddingSchemaHistoryCompactorJob.CompactionPolicy policy = job.loadPolicy(config);

        assertEquals(true, policy.enabled());
        assertEquals(77, policy.maxEvents());
        assertEquals(9, policy.maxAgeDays());
        assertEquals(true, policy.dryRun());
        assertEquals(Set.of("a", "b", "c"), policy.tenants());
    }

    @Test
    void shouldExposeCompactionRuntimeStatus() {
        when(schemaAdminService.tenantIdsWithHistory()).thenReturn(Set.of("ok-tenant", "bad-tenant"));
        when(schemaAdminService.compactHistory(eq("ok-tenant"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new EmbeddingSchemaHistoryCompactionStatus("ok-tenant", 10, 7, 3, false, java.time.Instant.now()));
        doThrow(new IllegalStateException("boom"))
                .when(schemaAdminService).compactHistory(eq("bad-tenant"), org.mockito.ArgumentMatchers.any());

        job.runCompactionCycle(new EmbeddingSchemaHistoryCompactorJob.CompactionPolicy(
                true,
                50,
                10,
                false,
                Set.of()));

        EmbeddingSchemaHistoryCompactorStatus status = job.status();
        assertEquals(1L, status.totalCycles());
        assertEquals(1L, status.totalTenantsProcessed());
        assertEquals(1L, status.totalFailures());
        assertEquals(1, status.lastCycleTenantsProcessed());
        assertEquals(3L, status.lastCycleRemovedCount());
        assertNotNull(status.lastCycleStartedAt());
        assertNotNull(status.lastCycleFinishedAt());
        assertEquals(1.0, meterRegistry.find("wayang.rag.embedding.schema.compaction.cycle.count").counter().count());
        assertEquals(1.0, meterRegistry.find("wayang.rag.embedding.schema.compaction.tenants_processed.count").counter().count());
        assertEquals(3.0, meterRegistry.find("wayang.rag.embedding.schema.compaction.removed.count").counter().count());
        assertEquals(1.0, meterRegistry.find("wayang.rag.embedding.schema.compaction.failure.count").counter().count());
    }

    @SuppressWarnings("unchecked")
    private void mockOptionalValue(Config config, Map<String, Object> values) {
        when(config.getOptionalValue(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0, String.class);
                    Class<?> type = invocation.getArgument(1, Class.class);
                    Object value = values.get(key);
                    if (value == null) {
                        return Optional.empty();
                    }
                    if (type.isInstance(value)) {
                        return Optional.of(value);
                    }
                    if (type == Integer.class) {
                        return Optional.of(Integer.valueOf(String.valueOf(value)));
                    }
                    if (type == Boolean.class) {
                        return Optional.of(Boolean.valueOf(String.valueOf(value)));
                    }
                    return Optional.of(String.valueOf(value));
                });
    }
}
