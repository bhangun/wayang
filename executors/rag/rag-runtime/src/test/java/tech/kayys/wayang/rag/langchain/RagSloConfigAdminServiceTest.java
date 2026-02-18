package tech.kayys.gamelan.executor.rag.langchain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RagSloConfigAdminServiceTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("wayang.rag.slo.embedding-latency-p95-ms");
        System.clearProperty("wayang.rag.slo.search-latency-p95-ms");
        System.clearProperty("wayang.rag.slo.ingest-latency-p95-ms");
        System.clearProperty("wayang.rag.slo.embedding-failure-rate");
        System.clearProperty("wayang.rag.slo.search-failure-rate");
        System.clearProperty("wayang.rag.slo.index-lag-ms");
        System.clearProperty("wayang.rag.slo.compaction-failure-rate");
        System.clearProperty("wayang.rag.slo.compaction-cycle-staleness-ms");
        System.clearProperty("wayang.rag.slo.severity.warning-multiplier");
        System.clearProperty("wayang.rag.slo.severity.critical-multiplier");
        System.clearProperty("wayang.rag.slo.severity.warning-by-metric");
        System.clearProperty("wayang.rag.slo.severity.critical-by-metric");
        System.clearProperty("wayang.rag.slo.alert.enabled");
        System.clearProperty("wayang.rag.slo.alert.min-severity");
        System.clearProperty("wayang.rag.slo.alert.cooldown-ms");
    }

    @Test
    void shouldUpdateThresholdsLive() {
        RagRuntimeConfig config = new RagRuntimeConfig();
        RagSloConfigAdminService service = new RagSloConfigAdminService();
        service.config = config;

        RagSloConfigStatus status = service.update(new RagSloConfigUpdate(
                123.0,
                234.0,
                345.0,
                0.12,
                0.34,
                567L,
                0.45,
                890L,
                1.4,
                2.8,
                java.util.Map.of("embedding_latency_p95_ms", 1.7),
                java.util.Map.of("embedding_latency_p95_ms", 3.5),
                true,
                "critical",
                45000L));

        assertEquals(123.0, status.thresholds().embeddingLatencyP95Ms());
        assertEquals(234.0, status.thresholds().searchLatencyP95Ms());
        assertEquals(345.0, status.thresholds().ingestLatencyP95Ms());
        assertEquals(0.12, status.thresholds().embeddingFailureRate());
        assertEquals(0.34, status.thresholds().searchFailureRate());
        assertEquals(567L, status.thresholds().indexLagMs());
        assertEquals(0.45, status.thresholds().compactionFailureRate());
        assertEquals(890L, status.thresholds().compactionCycleStalenessMs());
        assertEquals(1.4, status.thresholds().severityWarningMultiplier());
        assertEquals(2.8, status.thresholds().severityCriticalMultiplier());
        assertEquals(1.7, status.thresholds().severityWarningByMetric().get("embedding_latency_p95_ms"));
        assertEquals(3.5, status.thresholds().severityCriticalByMetric().get("embedding_latency_p95_ms"));
        assertEquals(true, status.thresholds().alertEnabled());
        assertEquals("critical", status.thresholds().alertMinSeverity());
        assertEquals(45000L, status.thresholds().alertCooldownMs());
    }

    @Test
    void shouldReloadThresholdsFromProperties() {
        System.setProperty("wayang.rag.slo.embedding-latency-p95-ms", "777");
        System.setProperty("wayang.rag.slo.search-latency-p95-ms", "888");
        System.setProperty("wayang.rag.slo.ingest-latency-p95-ms", "999");
        System.setProperty("wayang.rag.slo.embedding-failure-rate", "0.22");
        System.setProperty("wayang.rag.slo.search-failure-rate", "0.33");
        System.setProperty("wayang.rag.slo.index-lag-ms", "4444");
        System.setProperty("wayang.rag.slo.compaction-failure-rate", "0.44");
        System.setProperty("wayang.rag.slo.compaction-cycle-staleness-ms", "5555");
        System.setProperty("wayang.rag.slo.severity.warning-multiplier", "1.6");
        System.setProperty("wayang.rag.slo.severity.critical-multiplier", "3.2");
        System.setProperty("wayang.rag.slo.severity.warning-by-metric", "index_lag_ms=2.1,search_latency_p95_ms=1.8");
        System.setProperty("wayang.rag.slo.severity.critical-by-metric", "index_lag_ms=4.4,search_latency_p95_ms=2.9");
        System.setProperty("wayang.rag.slo.alert.enabled", "false");
        System.setProperty("wayang.rag.slo.alert.min-severity", "critical");
        System.setProperty("wayang.rag.slo.alert.cooldown-ms", "75000");

        RagRuntimeConfig config = new RagRuntimeConfig();
        RagSloConfigAdminService service = new RagSloConfigAdminService();
        service.config = config;

        RagSloConfigStatus status = service.reload();

        assertEquals(777.0, status.thresholds().embeddingLatencyP95Ms());
        assertEquals(888.0, status.thresholds().searchLatencyP95Ms());
        assertEquals(999.0, status.thresholds().ingestLatencyP95Ms());
        assertEquals(0.22, status.thresholds().embeddingFailureRate());
        assertEquals(0.33, status.thresholds().searchFailureRate());
        assertEquals(4444L, status.thresholds().indexLagMs());
        assertEquals(0.44, status.thresholds().compactionFailureRate());
        assertEquals(5555L, status.thresholds().compactionCycleStalenessMs());
        assertEquals(1.6, status.thresholds().severityWarningMultiplier());
        assertEquals(3.2, status.thresholds().severityCriticalMultiplier());
        assertEquals(2.1, status.thresholds().severityWarningByMetric().get("index_lag_ms"));
        assertEquals(4.4, status.thresholds().severityCriticalByMetric().get("index_lag_ms"));
        assertEquals(false, status.thresholds().alertEnabled());
        assertEquals("critical", status.thresholds().alertMinSeverity());
        assertEquals(75000L, status.thresholds().alertCooldownMs());
    }
}
