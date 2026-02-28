package tech.kayys.gamelan.executor.rag.langchain;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.kayys.wayang.rag.RagObservabilityMetrics;
import tech.kayys.wayang.rag.RagRuntimeConfig;
import tech.kayys.wayang.rag.retrieval.RagRetrievalEvalGuardrailBreach;
import tech.kayys.wayang.rag.retrieval.RagRetrievalEvalGuardrailService;
import tech.kayys.wayang.rag.retrieval.RagRetrievalEvalGuardrailStatus;
import tech.kayys.wayang.rag.slo.RagSloAdminService;
import tech.kayys.wayang.rag.slo.RagSloStatus;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagSloAdminServiceTest {

    @Test
    void shouldReportHealthyWhenMetricsUnderThreshold() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 2, 10);
        metrics.recordSearchSuccess("tenant-a", 15, 3);
        metrics.recordIngestion("tenant-a", 1, 2, 20);

        RagRuntimeConfig config = new RagRuntimeConfig();
        config.setSloEmbeddingLatencyP95Ms(100);
        config.setSloSearchLatencyP95Ms(200);
        config.setSloIngestLatencyP95Ms(300);
        config.setSloEmbeddingFailureRate(0.5);
        config.setSloSearchFailureRate(0.5);
        config.setSloIndexLagMs(1000);

        EmbeddingSchemaHistoryCompactorJob compactorJob = mock(EmbeddingSchemaHistoryCompactorJob.class);
        when(compactorJob.status()).thenReturn(new EmbeddingSchemaHistoryCompactorStatus(
                false,
                200,
                30,
                false,
                Set.of(),
                null,
                null,
                0,
                0,
                0,
                0,
                0,
                null));

        RagSloStatus status = new RagSloAdminService(config, metrics, compactorJob).status();
        assertTrue(status.healthy());
        assertTrue(status.breaches().isEmpty());
    }

    @Test
    void shouldReportBreachesWhenThresholdExceeded() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 1, 100);
        metrics.recordEmbeddingFailure("hash-384");
        metrics.recordSearchSuccess("tenant-a", 120, 2);
        metrics.recordSearchFailure("tenant-a");
        metrics.recordIngestion("tenant-a", 1, 1, 500);

        RagRuntimeConfig config = new RagRuntimeConfig();
        config.setSloEmbeddingLatencyP95Ms(80);
        config.setSloSearchLatencyP95Ms(50);
        config.setSloIngestLatencyP95Ms(300);
        config.setSloEmbeddingFailureRate(0.40);
        config.setSloSearchFailureRate(0.40);
        config.setSloIndexLagMs(400);

        EmbeddingSchemaHistoryCompactorJob compactorJob = mock(EmbeddingSchemaHistoryCompactorJob.class);
        when(compactorJob.status()).thenReturn(new EmbeddingSchemaHistoryCompactorStatus(
                true,
                200,
                30,
                false,
                Set.of("tenant-a"),
                Instant.now().minusSeconds(10_000),
                Instant.now().minusSeconds(10_000),
                1,
                1,
                1,
                1,
                0,
                "failure"));
        config.setSloCompactionFailureRate(0.2);
        config.setSloCompactionCycleStalenessMs(1000);

        RagSloStatus status = new RagSloAdminService(config, metrics, compactorJob).status();
        assertFalse(status.healthy());
        assertTrue(status.breaches().size() >= 3);
        assertTrue(status.breaches().stream().anyMatch(b -> b.severity().equals("warning")));
        assertTrue(status.breaches().stream().anyMatch(b -> b.severity().equals("critical")));
        assertTrue(status.breaches().stream().anyMatch(b -> b.metric().equals("compaction_failure_rate")));
        assertTrue(status.breaches().stream().anyMatch(b -> b.metric().equals("compaction_cycle_staleness_ms")));
    }

    @Test
    void shouldRespectSeverityMultipliers() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 1, 100);
        metrics.recordSearchSuccess("tenant-a", 20, 2);
        metrics.recordIngestion("tenant-a", 1, 1, 100);

        RagRuntimeConfig config = new RagRuntimeConfig();
        config.setSloEmbeddingLatencyP95Ms(80);
        config.setSloSearchLatencyP95Ms(100);
        config.setSloIngestLatencyP95Ms(200);
        config.setSloEmbeddingFailureRate(1.0);
        config.setSloSearchFailureRate(1.0);
        config.setSloIndexLagMs(500);
        config.setSloSeverityWarningMultiplier(1.5);
        config.setSloSeverityCriticalMultiplier(3.0);

        EmbeddingSchemaHistoryCompactorJob compactorJob = mock(EmbeddingSchemaHistoryCompactorJob.class);
        when(compactorJob.status()).thenReturn(new EmbeddingSchemaHistoryCompactorStatus(
                false,
                200,
                30,
                false,
                Set.of(),
                null,
                null,
                0,
                0,
                0,
                0,
                0,
                null));

        RagSloStatus status = new RagSloAdminService(config, metrics, compactorJob).status();
        assertTrue(status.healthy());
        assertTrue(status.breaches().isEmpty());
    }

    @Test
    void shouldApplyPerMetricSeverityMultipliers() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 1, 130);

        RagRuntimeConfig config = new RagRuntimeConfig();
        config.setSloEmbeddingLatencyP95Ms(80);
        config.setSloSearchLatencyP95Ms(1000);
        config.setSloIngestLatencyP95Ms(1000);
        config.setSloEmbeddingFailureRate(1.0);
        config.setSloSearchFailureRate(1.0);
        config.setSloIndexLagMs(5000);
        config.setSloSeverityWarningMultiplier(1.2);
        config.setSloSeverityCriticalMultiplier(2.0);

        EmbeddingSchemaHistoryCompactorJob compactorJob = mock(EmbeddingSchemaHistoryCompactorJob.class);
        when(compactorJob.status()).thenReturn(new EmbeddingSchemaHistoryCompactorStatus(
                false,
                200,
                30,
                false,
                Set.of(),
                null,
                null,
                0,
                0,
                0,
                0,
                0,
                null));

        RagSloStatus baseline = new RagSloAdminService(config, metrics, compactorJob).status();
        double observed = baseline.snapshot().embeddingLatencyP95Ms();
        double ratio = observed / config.getSloEmbeddingLatencyP95Ms();
        assertTrue(ratio > 1.0);

        config.setSloSeverityWarningByMetric(java.util.Map.of("embedding_latency_p95_ms", ratio + 0.5));
        config.setSloSeverityCriticalByMetric(java.util.Map.of("embedding_latency_p95_ms", ratio + 0.6));
        RagSloStatus status = new RagSloAdminService(config, metrics, compactorJob).status();
        assertTrue(status.breaches().stream().noneMatch(b -> b.metric().equals("embedding_latency_p95_ms")));

        config.setSloSeverityWarningByMetric(java.util.Map.of("embedding_latency_p95_ms", Math.max(1.0, ratio - 0.2)));
        config.setSloSeverityCriticalByMetric(java.util.Map.of("embedding_latency_p95_ms", Math.max(1.0, ratio - 0.1)));
        status = new RagSloAdminService(config, metrics, compactorJob).status();
        assertTrue(status.breaches().stream().anyMatch(b ->
                b.metric().equals("embedding_latency_p95_ms") && b.severity().equals("critical")));
    }

    @Test
    void shouldSurfaceEvalGuardrailBreachesIntoSloStatus() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 1, 10);

        RagRuntimeConfig config = new RagRuntimeConfig();
        config.setSloEmbeddingLatencyP95Ms(1000);
        config.setSloSearchLatencyP95Ms(1000);
        config.setSloIngestLatencyP95Ms(1000);
        config.setSloEmbeddingFailureRate(1.0);
        config.setSloSearchFailureRate(1.0);
        config.setSloIndexLagMs(10000);

        EmbeddingSchemaHistoryCompactorJob compactorJob = mock(EmbeddingSchemaHistoryCompactorJob.class);
        when(compactorJob.status()).thenReturn(new EmbeddingSchemaHistoryCompactorStatus(
                false,
                200,
                30,
                false,
                Set.of(),
                null,
                null,
                0,
                0,
                0,
                0,
                0,
                null));

        RagRetrievalEvalGuardrailService guardrailService = mock(RagRetrievalEvalGuardrailService.class);
        when(guardrailService.evaluate(null, null, null)).thenReturn(new RagRetrievalEvalGuardrailStatus(
                true,
                false,
                "tenant-a",
                "dataset-a",
                20,
                2,
                "regression_detected",
                List.of(new RagRetrievalEvalGuardrailBreach("mrr_delta", -0.2, -0.1, "drop too large")),
                null,
                Instant.now()));

        RagSloStatus status = new RagSloAdminService(config, metrics, compactorJob, guardrailService).status();
        assertFalse(status.healthy());
        assertTrue(status.breaches().stream().anyMatch(b -> b.metric().equals("eval_guardrail_breach_count")));
        assertTrue(status.breaches().stream().anyMatch(b -> b.metric().equals("eval_guardrail_mrr_delta")));
    }
}
