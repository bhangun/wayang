package tech.kayys.wayang.runtime.standalone.status;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService;
import tech.kayys.wayang.control.service.AgentManager;
import tech.kayys.wayang.control.service.PluginManagerService;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.service.SchemaRegistryService;
import tech.kayys.wayang.control.service.WorkflowManager;
import tech.kayys.wayang.inference.gollek.GollekInferenceService;
import tech.kayys.wayang.schema.validator.SchemaValidationService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects readiness view across Wayang (control), Gamelan (orchestration), and Gollek (inference).
 */
@ApplicationScoped
public class RuntimeStatusService {

    private static final TenantId DEFAULT_TENANT = TenantId.of("default-tenant");

    @Inject Instance<ProjectManager> projectManager;
    @Inject Instance<WorkflowManager> workflowManager;
    @Inject Instance<AgentManager> agentManager;
    @Inject Instance<SchemaRegistryService> schemaRegistryService;
    @Inject Instance<SchemaValidationService> schemaValidationService;
    @Inject Instance<PluginManagerService> pluginManagerService;
    @Inject Instance<WorkflowDefinitionService> workflowDefinitionService;
    @Inject Instance<GollekInferenceService> gollekInferenceService;

    public Uni<RuntimeStatusSnapshot> collectStatus() {
        Uni<RuntimeComponentStatus> gamelan = gamelanStatus();

        return gamelan.map(gamelanStatus -> {
            Map<String, RuntimeComponentStatus> components = new LinkedHashMap<>();

            boolean controlAvailable = isAvailable(projectManager)
                    && isAvailable(workflowManager)
                    && isAvailable(agentManager);
            components.put(
                    "wayang.control",
                    new RuntimeComponentStatus(controlAvailable, controlAvailable,
                            controlAvailable ? "core managers ready" : "missing control beans"));

            boolean schemaAvailable = isAvailable(schemaRegistryService) && isAvailable(schemaValidationService);
            components.put(
                    "wayang.schema",
                    new RuntimeComponentStatus(schemaAvailable, schemaAvailable,
                            schemaAvailable ? "registry + validator ready" : "schema services not resolvable"));

            boolean pluginAvailable = isAvailable(pluginManagerService);
            components.put(
                    "wayang.plugins",
                    new RuntimeComponentStatus(pluginAvailable, pluginAvailable,
                            pluginAvailable ? "plugin manager ready" : "plugin manager unavailable"));

            boolean gollekAvailable = isAvailable(gollekInferenceService)
                    && gollekInferenceService.get().isAvailable();
            components.put(
                    "gollek.inference",
                    new RuntimeComponentStatus(gollekAvailable, gollekAvailable,
                            gollekAvailable ? "inference service resolvable" : "inference engine unavailable"));

            components.put("gamelan.orchestration", gamelanStatus);

            boolean ready = components.values().stream().allMatch(RuntimeComponentStatus::healthy);
            return new RuntimeStatusSnapshot(ready, components);
        });
    }

    private Uni<RuntimeComponentStatus> gamelanStatus() {
        if (!isAvailable(workflowDefinitionService)) {
            return Uni.createFrom()
                    .item(new RuntimeComponentStatus(false, false, "workflow definition service unavailable"));
        }

        return workflowDefinitionService.get().list(DEFAULT_TENANT, true)
                .onItem().transform(definitions -> new RuntimeComponentStatus(
                        true,
                        true,
                        "reachable (definitions=" + definitions.size() + ")"))
                .onFailure().recoverWithItem(failure -> new RuntimeComponentStatus(
                        true,
                        false,
                        "unhealthy: " + failure.getClass().getSimpleName()));
    }

    private static boolean isAvailable(Instance<?> instance) {
        return instance != null && instance.isResolvable();
    }
}
