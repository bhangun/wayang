package tech.kayys.wayang.eip.plugin;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record IntegrationDeployment(
        String deploymentId,
        String pluginId,
        List<String> routeIds,
        Map<String, Object> options,
        Instant deployedAt) {
}
