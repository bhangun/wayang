package tech.kayys.gamelan.executor.rag.langchain;

import java.util.List;

public interface RagPluginSelectionStrategy {

    String id();

    RagPluginTenantStrategyResolution resolve(String tenantId, RagRuntimeConfig config);

    List<RagPipelinePlugin> selectActivePlugins(
            List<RagPipelinePlugin> discovered,
            String tenantId,
            RagRuntimeConfig config,
            RagPluginTenantStrategyResolution resolution);
}
