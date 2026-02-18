package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record EmbeddingSchemaMigrationStatus(
        String tenantId,
        EmbeddingSchemaContract previous,
        EmbeddingSchemaContract current,
        boolean changed,
        boolean clearedNamespace,
        boolean dryRun,
        Instant migratedAt) {
}
