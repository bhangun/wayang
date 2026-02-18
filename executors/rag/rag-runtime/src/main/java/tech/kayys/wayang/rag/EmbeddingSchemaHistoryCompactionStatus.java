package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record EmbeddingSchemaHistoryCompactionStatus(
        String tenantId,
        int beforeCount,
        int afterCount,
        int removedCount,
        boolean dryRun,
        Instant compactedAt) {
}
