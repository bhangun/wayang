package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;
import java.util.List;

public record RagPluginAdminStatus(
        String tenantId,
        RagPluginTenantStrategyResolution strategy,
        List<RagPluginManager.PluginInspection> plugins,
        List<String> activePluginIds,
        Instant observedAt) {
}
