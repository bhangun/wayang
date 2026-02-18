package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;
import java.util.Set;

public record EmbeddingSchemaHistoryCompactorStatus(
        boolean enabled,
        int maxEvents,
        int maxAgeDays,
        boolean dryRun,
        Set<String> configuredTenants,
        Instant lastCycleStartedAt,
        Instant lastCycleFinishedAt,
        long totalCycles,
        long totalTenantsProcessed,
        long totalFailures,
        int lastCycleTenantsProcessed,
        long lastCycleRemovedCount,
        String lastError) {
}
