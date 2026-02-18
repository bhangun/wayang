package tech.kayys.wayang.eip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.config.IntegrationConfig;
import tech.kayys.wayang.eip.plugin.IntegrationDeployment;
import tech.kayys.wayang.eip.plugin.IntegrationPlugin;
import tech.kayys.wayang.eip.plugin.IntegrationPluginContext;
import tech.kayys.wayang.eip.plugin.IntegrationPluginRegistry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationService.class);

    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;
    private final IntegrationPluginRegistry pluginRegistry;
    private final IntegrationConfig integrationConfig;

    private final Map<String, IntegrationDeployment> deploymentsById = new ConcurrentHashMap<>();

    @Inject
    public IntegrationService(
            CamelContext camelContext,
            ProducerTemplate producerTemplate,
            ObjectMapper objectMapper,
            IntegrationPluginRegistry pluginRegistry,
            IntegrationConfig integrationConfig) {
        this.camelContext = camelContext;
        this.producerTemplate = producerTemplate;
        this.objectMapper = objectMapper;
        this.pluginRegistry = pluginRegistry;
        this.integrationConfig = integrationConfig;
    }

    public List<IntegrationPlugin> listPlugins() {
        return pluginRegistry.all();
    }

    public List<IntegrationDeployment> listDeployments() {
        return deploymentsById.values().stream()
                .sorted((a, b) -> a.deployedAt().compareTo(b.deployedAt()))
                .toList();
    }

    public IntegrationDeployment deploy(String pluginId, Map<String, Object> options) {
        IntegrationPlugin plugin = pluginRegistry.find(pluginId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown integration plugin: " + pluginId));

        Map<String, Object> safeOptions = options == null ? Collections.emptyMap() : new LinkedHashMap<>(options);
        IntegrationPluginContext context = new IntegrationPluginContext(camelContext, producerTemplate, objectMapper);

        try {
            IntegrationDeployment deployment = plugin.deploy(context, safeOptions);
            deploymentsById.put(deployment.deploymentId(), deployment);
            return deployment;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deploy plugin " + pluginId + ": " + e.getMessage(), e);
        }
    }

    public boolean undeploy(String deploymentId) {
        IntegrationDeployment deployment = deploymentsById.remove(deploymentId);
        if (deployment == null) {
            return false;
        }

        Optional<IntegrationPlugin> pluginOpt = pluginRegistry.find(deployment.pluginId());
        if (pluginOpt.isEmpty()) {
            LOG.warn("Plugin {} not found during undeploy for {}. Attempting direct route cleanup.",
                    deployment.pluginId(), deploymentId);
            return undeployFallback(deployment);
        }

        try {
            pluginOpt.get().undeploy(new IntegrationPluginContext(camelContext, producerTemplate, objectMapper), deployment);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to undeploy {} via plugin {}", deploymentId, deployment.pluginId(), e);
            return undeployFallback(deployment);
        }
    }

    public void deployConfiguredModules() {
        if (!integrationConfig.autoDeployEnabled()) {
            LOG.info("Auto deploy disabled by configuration.");
            return;
        }

        List<String> modules = parseModules(integrationConfig.enabledModules());
        for (String moduleId : modules) {
            boolean alreadyDeployed = deploymentsById.values().stream()
                    .anyMatch(d -> normalize(d.pluginId()).equals(moduleId));
            if (alreadyDeployed) {
                continue;
            }

            LOG.info("Auto deploying integration plugin: {}", moduleId);
            deploy(moduleId, Collections.emptyMap());
        }
    }

    private boolean undeployFallback(IntegrationDeployment deployment) {
        boolean ok = true;
        for (String routeId : deployment.routeIds()) {
            try {
                if (camelContext.getRoute(routeId) != null) {
                    camelContext.getRouteController().stopRoute(routeId);
                    camelContext.removeRoute(routeId);
                }
            } catch (Exception e) {
                ok = false;
                LOG.error("Failed to stop/remove route {}", routeId, e);
            }
        }
        return ok;
    }

    private List<String> parseModules(String modulesCsv) {
        if (modulesCsv == null || modulesCsv.isBlank()) {
            return List.of();
        }

        String[] raw = modulesCsv.split(",");
        List<String> modules = new ArrayList<>();
        for (String value : raw) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                modules.add(normalized);
            }
        }
        return modules;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
