package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;
import java.util.Map;

public record EmbeddingConfigStatus(
        String defaultProvider,
        String defaultModel,
        String embeddingVersion,
        boolean normalize,
        Map<String, TenantEmbeddingStrategyStatus> tenantStrategies,
        Instant refreshedAt) {

    public record TenantEmbeddingStrategyStatus(String provider, String model) {
    }
}
