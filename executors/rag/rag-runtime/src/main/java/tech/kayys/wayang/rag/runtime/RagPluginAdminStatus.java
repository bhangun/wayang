package tech.kayys.wayang.rag.runtime;
import tech.kayys.wayang.rag.plugin.api.*;
import tech.kayys.wayang.rag.core.*;

import java.time.Instant;
import java.util.List;

public record RagPluginAdminStatus(
        String tenantId,
        RagPluginTenantStrategyResolution strategy,
        List<RagPluginManager.PluginInspection> plugins,
        List<String> activePluginIds,
        Instant observedAt) {
}
