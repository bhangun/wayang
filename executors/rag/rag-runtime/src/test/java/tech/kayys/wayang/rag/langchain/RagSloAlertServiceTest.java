package tech.kayys.gamelan.executor.rag.langchain;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tech.kayys.wayang.rag.RagObservabilityMetrics;
import tech.kayys.wayang.rag.RagRuntimeConfig;
import tech.kayys.wayang.rag.slo.RagSloAdminService;
import tech.kayys.wayang.rag.slo.RagSloAlertService;
import tech.kayys.wayang.rag.slo.RagSloAlertSnoozeRequest;
import tech.kayys.wayang.rag.slo.RagSloAlertSnoozeStatus;
import tech.kayys.wayang.rag.slo.RagSloAlertState;
import tech.kayys.wayang.rag.slo.RagSloBreach;
import tech.kayys.wayang.rag.slo.RagSloSnapshot;
import tech.kayys.wayang.rag.slo.RagSloStatus;
import tech.kayys.wayang.rag.slo.RagSloThresholds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagSloAlertServiceTest {

    @Test
    void shouldAlertThenSuppressWithinCooldown() {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(true);
        config.setSloAlertMinSeverity("warning");
        config.setSloAlertCooldownMs(60000L);

        RagSloAdminService sloService = createBreachingSloService(config);
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RagSloAlertService service = new RagSloAlertService(config, sloService, clock);

        RagSloAlertState first = service.evaluate();
        assertTrue(first.shouldAlert());
        assertFalse(first.snoozed());
        assertEquals("alert_ready", first.reason());

        RagSloAlertState second = service.evaluate();
        assertFalse(second.shouldAlert());
        assertEquals("suppressed_by_cooldown", second.reason());
        assertTrue(second.cooldownRemainingMs() > 0);

        clock.advanceSeconds(61);
        RagSloAlertState third = service.evaluate();
        assertTrue(third.shouldAlert());
        assertEquals("alert_ready", third.reason());
    }

    @Test
    void shouldFilterByMinSeverity() {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(true);
        config.setSloAlertMinSeverity("critical");
        config.setSloAlertCooldownMs(60000L);
        config.setSloEmbeddingLatencyP95Ms(100);
        config.setSloSeverityWarningMultiplier(1.0);
        config.setSloSeverityCriticalMultiplier(2.0);

        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 1, 130);

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

        RagSloAdminService sloService = new RagSloAdminService(config, metrics, compactorJob);
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RagSloAlertService service = new RagSloAlertService(config, sloService, clock);

        RagSloAlertState state = service.evaluate();
        assertFalse(state.shouldAlert());
        assertFalse(state.snoozed());
        assertEquals("no_qualifying_breaches", state.reason());
    }

    @Test
    void shouldNotAlertWhenDisabled() {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(false);

        RagSloAdminService sloService = createBreachingSloService(config);
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RagSloAlertService service = new RagSloAlertService(config, sloService, clock);

        RagSloAlertState state = service.evaluate();
        assertFalse(state.shouldAlert());
        assertFalse(state.snoozed());
        assertEquals("alerting_disabled", state.reason());
        assertFalse(state.alertingEnabled());
    }

    @Test
    void shouldSuppressWithAllScopeSnooze() {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(true);
        config.setSloAlertMinSeverity("warning");
        config.setSloAlertCooldownMs(0L);

        RagSloAdminService sloService = createBreachingSloService(config);
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RagSloAlertService service = new RagSloAlertService(config, sloService, clock);

        RagSloAlertSnoozeStatus snoozed = service.snooze(new RagSloAlertSnoozeRequest(60000L, "all"));
        assertTrue(snoozed.active());

        RagSloAlertState state = service.evaluate();
        assertFalse(state.shouldAlert());
        assertTrue(state.snoozed());
        assertEquals("suppressed_by_snooze", state.reason());

        clock.advanceSeconds(61);
        state = service.evaluate();
        assertTrue(state.shouldAlert());
        assertFalse(state.snoozed());
    }

    @Test
    void shouldOnlySuppressGuardrailScopeWhenOnlyGuardrailBreachesActive() {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(true);
        config.setSloAlertCooldownMs(0L);
        config.setSloAlertMinSeverity("warning");

        RagSloAdminService sloService = mock(RagSloAdminService.class);
        when(sloService.status())
                .thenReturn(statusWithBreaches(List.of(
                        breach("eval_guardrail_mrr_delta", "critical"),
                        breach("eval_guardrail_recall_at_k_delta", "critical"))))
                .thenReturn(statusWithBreaches(List.of(
                        breach("eval_guardrail_mrr_delta", "critical"),
                        breach("eval_guardrail_recall_at_k_delta", "critical"))))
                .thenReturn(statusWithBreaches(List.of(
                        breach("eval_guardrail_mrr_delta", "critical"),
                        breach("embedding_latency_p95_ms", "critical"))));

        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RagSloAlertService service = new RagSloAlertService(config, sloService, clock);

        RagSloAlertSnoozeStatus snoozed = service.snooze(new RagSloAlertSnoozeRequest(60000L, "guardrail"));
        assertTrue(snoozed.active());
        assertEquals("guardrail", snoozed.scope());

        RagSloAlertState first = service.evaluate();
        assertFalse(first.shouldAlert());
        assertTrue(first.snoozed());
        assertEquals("suppressed_by_snooze", first.reason());

        RagSloAlertState second = service.evaluate();
        assertTrue(second.shouldAlert());
        assertFalse(second.snoozed());
    }

    @Test
    void shouldClearSnooze() {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(true);
        config.setSloAlertCooldownMs(0L);
        config.setSloAlertMinSeverity("warning");

        RagSloAlertService service = new RagSloAlertService(config, createBreachingSloService(config), new MutableClock(Instant.parse("2026-01-01T00:00:00Z")));
        service.snooze(new RagSloAlertSnoozeRequest(60000L, "all"));
        RagSloAlertSnoozeStatus cleared = service.clearSnooze();
        assertFalse(cleared.active());

        RagSloAlertState state = service.evaluate();
        assertTrue(state.shouldAlert());
    }

    @Test
    void shouldPersistSnoozeAcrossServiceRestart(@TempDir Path tempDir) {
        RagRuntimeConfig config = baseConfig();
        config.setSloAlertEnabled(true);
        config.setSloAlertCooldownMs(0L);
        config.setSloAlertSnoozePath(tempDir.resolve("slo-alert-snooze.json").toString());

        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RagSloAlertService first = new RagSloAlertService(config, createBreachingSloService(config), clock);
        RagSloAlertSnoozeStatus snoozed = first.snooze(new RagSloAlertSnoozeRequest(60000L, "all"));
        assertTrue(snoozed.active());

        RagSloAlertService second = new RagSloAlertService(config, createBreachingSloService(config), clock);
        RagSloAlertSnoozeStatus status = second.snoozeStatus();
        assertTrue(status.active());
        assertEquals("all", status.scope());

        second.clearSnooze();

        RagSloAlertService third = new RagSloAlertService(config, createBreachingSloService(config), clock);
        assertFalse(third.snoozeStatus().active());
    }

    private static RagSloBreach breach(String metric, String severity) {
        return new RagSloBreach(metric, 1.0, 0.0, severity, "test");
    }

    private static RagSloStatus statusWithBreaches(List<RagSloBreach> breaches) {
        return new RagSloStatus(
                breaches.isEmpty(),
                new RagSloThresholds(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, java.util.Map.of(), java.util.Map.of(), true, "warning", 1000),
                new RagSloSnapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                breaches,
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private static RagRuntimeConfig baseConfig() {
        RagRuntimeConfig config = new RagRuntimeConfig();
        config.setSloEmbeddingLatencyP95Ms(80);
        config.setSloSearchLatencyP95Ms(1000);
        config.setSloIngestLatencyP95Ms(1000);
        config.setSloEmbeddingFailureRate(1.0);
        config.setSloSearchFailureRate(1.0);
        config.setSloIndexLagMs(5000);
        config.setSloSeverityWarningMultiplier(1.0);
        config.setSloSeverityCriticalMultiplier(2.0);
        return config;
    }

    private static RagSloAdminService createBreachingSloService(RagRuntimeConfig config) {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        RagObservabilityMetrics metrics = new RagObservabilityMetrics(meterRegistry);
        metrics.initGauge();
        metrics.recordEmbeddingSuccess("hash-384", 1, 130);

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

        return new RagSloAdminService(config, metrics, compactorJob);
    }

    private static final class MutableClock extends Clock {
        private Instant current;

        private MutableClock(Instant current) {
            this.current = current;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }

        void advanceSeconds(long seconds) {
            current = current.plusSeconds(seconds);
        }
    }
}
