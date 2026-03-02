package tech.kayys.wayang.rag.runtime;
import tech.kayys.wayang.rag.plugin.api.*;
import tech.kayys.wayang.rag.core.*;

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
