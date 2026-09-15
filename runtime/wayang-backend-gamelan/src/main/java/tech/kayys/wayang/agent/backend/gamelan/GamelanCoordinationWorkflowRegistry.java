package tech.kayys.wayang.agent.backend.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gamelan.engine.node.NodeDefinition;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.node.NodeType;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionId;
import tech.kayys.gamelan.engine.workflow.WorkflowMetadata;
import tech.kayys.gamelan.engine.workflow.WorkflowMode;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.wayang.coordination.CoordinationWorkflowDefinition;
import tech.kayys.wayang.coordination.CoordinationWorkflowDefinitionRegistry;
import tech.kayys.wayang.coordination.DefaultWorkflowDefinitionCanonicalizer;
import tech.kayys.wayang.coordination.WorkflowDefinitionCanonicalizer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gamelan-backed implementation of {@link CoordinationWorkflowDefinitionRegistry}.
 * Translates compiled {@link CoordinationWorkflowDefinition} IR into native Gamelan {@link WorkflowDefinition}
 * and registers it with Gamelan, leveraging SHA-256 fingerprinting for deterministic deduplication.
 */
@ApplicationScoped
public class GamelanCoordinationWorkflowRegistry implements CoordinationWorkflowDefinitionRegistry {

    private static final Logger LOG = Logger.getLogger(GamelanCoordinationWorkflowRegistry.class);
    private static final String DEFAULT_VERSION = "1.0.0";
    public static final String FINGERPRINT_LABEL = "wayang.coordination.fingerprint";

    @Inject
    Instance<WorkflowDefinitionCanonicalizer> canonicalizerInstances;

    @Inject
    Instance<GamelanClient> clientInstances;

    private WorkflowDefinitionCanonicalizer canonicalizer;
    private GamelanClient gamelanClient;

    public GamelanCoordinationWorkflowRegistry() {
    }

    public GamelanCoordinationWorkflowRegistry(GamelanClient gamelanClient, WorkflowDefinitionCanonicalizer canonicalizer) {
        this.gamelanClient = gamelanClient;
        this.canonicalizer = canonicalizer;
    }

    @Override
    public Uni<String> getOrCreate(CoordinationWorkflowDefinition definition, String tenantId) {
        Objects.requireNonNull(definition, "definition must not be null");
        String effectiveTenant = tenantId != null && !tenantId.isBlank() ? tenantId : "default";

        WorkflowDefinitionCanonicalizer canon = resolveCanonicalizer();
        String fingerprint = canon.fingerprint(definition).value();

        GamelanClient client = resolveClient();
        if (client == null) {
            return Uni.createFrom().failure(new IllegalStateException("No GamelanClient available in runtime"));
        }

        return client.workflows()
                .getByFingerprint(definition.name(), DEFAULT_VERSION, fingerprint)
                .flatMap(existing -> {
                    if (existing != null && existing.id() != null) {
                        LOG.debugf("Reusing existing Gamelan workflow definition %s for fingerprint %s",
                                existing.id().value(), fingerprint);
                        return Uni.createFrom().item(existing.id().value());
                    }

                    LOG.infof("Deploying new Gamelan workflow definition for coordination %s (fingerprint=%s)",
                            definition.name(), fingerprint);

                    WorkflowDefinition gamelanDef = toGamelanDefinition(definition, fingerprint, effectiveTenant);
                    return client.workflows().create(gamelanDef)
                            .map(created -> created.id().value());
                });
    }

    private WorkflowDefinition toGamelanDefinition(
            CoordinationWorkflowDefinition def,
            String fingerprint,
            String tenantId) {

        List<NodeDefinition> nodeDefinitions = def.nodes().stream()
                .map(node -> {
                    Map<String, Object> config = new HashMap<>(node.parameters());
                    config.put("agentId", node.agentId());
                    config.put("operation", node.operation());

                    List<NodeId> dependsOn = node.dependsOn().stream()
                            .map(NodeId::of)
                            .toList();

                    return new NodeDefinition(
                            NodeId.of(node.id()),
                            node.id(),
                            NodeType.TASK,
                            WayangAgentNodeExecutor.EXECUTOR_TYPE,
                            config,
                            dependsOn,
                            List.of(),
                            RetryPolicy.none(),
                            Duration.ZERO,
                            false
                    );
                })
                .toList();

        Map<String, String> labels = new HashMap<>();
        labels.put(FINGERPRINT_LABEL, fingerprint);
        labels.put("tenantId", tenantId);
        labels.put("coordination.backend", def.backend());

        WorkflowMetadata metadata = WorkflowMetadata.system(labels);

        return WorkflowDefinition.builder()
                .id(WorkflowDefinitionId.generate())
                .tenantId(TenantId.of(tenantId))
                .name(def.name())
                .version(DEFAULT_VERSION)
                .description("Coordination workflow definition for " + def.name())
                .mode(WorkflowMode.FLOW)
                .nodes(nodeDefinitions)
                .metadata(metadata)
                .build();
    }

    private WorkflowDefinitionCanonicalizer resolveCanonicalizer() {
        if (canonicalizer != null) {
            return canonicalizer;
        }
        if (canonicalizerInstances != null && canonicalizerInstances.isResolvable()) {
            return canonicalizerInstances.get();
        }
        return new DefaultWorkflowDefinitionCanonicalizer();
    }

    private GamelanClient resolveClient() {
        if (gamelanClient != null) {
            return gamelanClient;
        }
        if (clientInstances != null && clientInstances.isResolvable()) {
            return clientInstances.get();
        }
        return null;
    }
}
