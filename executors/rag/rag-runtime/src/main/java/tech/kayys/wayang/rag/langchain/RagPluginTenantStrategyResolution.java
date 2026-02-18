package tech.kayys.gamelan.executor.rag.langchain;

public record RagPluginTenantStrategyResolution(
        String tenantId,
        String globalEnabledIds,
        String globalOrder,
        String tenantEnabledOverridesRaw,
        String tenantOrderOverridesRaw,
        String matchedTenantEnabledOverride,
        String matchedTenantOrderOverride,
        String effectiveEnabledIds,
        String effectiveOrder,
        String strategyId) {
}
