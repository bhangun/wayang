package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record RagPluginConfigStatus(
        RagPluginConfigSnapshot config,
        Instant updatedAt) {
}
