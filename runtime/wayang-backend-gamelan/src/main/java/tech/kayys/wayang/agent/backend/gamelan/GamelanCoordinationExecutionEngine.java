package tech.kayys.wayang.agent.backend.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.wayang.coordination.CoordinationContext;
import tech.kayys.wayang.coordination.CoordinationExecutionEngine;
import tech.kayys.wayang.coordination.CoordinationPlan;
import tech.kayys.wayang.coordination.CoordinationPlanCompiler;
import tech.kayys.wayang.coordination.CoordinationRegistry;
import tech.kayys.wayang.coordination.CoordinationRequest;
import tech.kayys.wayang.coordination.CoordinationResult;
import tech.kayys.wayang.coordination.CoordinationStrategies;
import tech.kayys.wayang.coordination.CoordinationStrategy;
import tech.kayys.wayang.coordination.CoordinationWorkflowDefinition;
import tech.kayys.wayang.coordination.CoordinationWorkflowDefinitionRegistry;
import tech.kayys.wayang.coordination.DefaultCoordinationPlanCompiler;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Gamelan-backed execution engine for multi-agent coordination.
 * Orchestrates: CoordinationRequest -> CoordinationStrategy.plan() -> PlanCompiler
 * -> WorkflowDefinitionRegistry.getOrCreate() -> Gamelan Run execution -> CoordinationResult.
 */
@ApplicationScoped
public class GamelanCoordinationExecutionEngine implements CoordinationExecutionEngine {

    private static final Logger LOG = Logger.getLogger(GamelanCoordinationExecutionEngine.class);

    @Inject
    Instance<CoordinationRegistry> coordinationRegistryInstances;

    @Inject
    Instance<CoordinationPlanCompiler> planCompilerInstances;

    @Inject
    Instance<CoordinationWorkflowDefinitionRegistry> definitionRegistryInstances;

    @Inject
    Instance<GamelanClient> clientInstances;

    private CoordinationRegistry coordinationRegistry;
    private CoordinationPlanCompiler planCompiler;
    private CoordinationWorkflowDefinitionRegistry definitionRegistry;
    private GamelanClient gamelanClient;

    public GamelanCoordinationExecutionEngine() {
    }

    public GamelanCoordinationExecutionEngine(
            CoordinationRegistry coordinationRegistry,
            CoordinationPlanCompiler planCompiler,
            CoordinationWorkflowDefinitionRegistry definitionRegistry,
            GamelanClient gamelanClient) {
        this.coordinationRegistry = coordinationRegistry;
        this.planCompiler = planCompiler;
        this.definitionRegistry = definitionRegistry;
        this.gamelanClient = gamelanClient;
    }

    @Override
    public Uni<CoordinationResult> coordinate(CoordinationRequest request, CoordinationContext context) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(context, "context must not be null");

        CoordinationRegistry registry = resolveCoordinationRegistry();
        String strategyId = (String) request.attributes().getOrDefault("strategy", CoordinationStrategies.CENTRALIZED);
        CoordinationStrategy strategy = registry != null ? registry.get(strategyId) : null;

        if (strategy == null) {
            return Uni.createFrom().item(new CoordinationResult(
                    context.executionId().toString(),
                    CoordinationResult.Status.FAILED,
                    Map.of(),
                    "No coordination strategy found for id: " + strategyId,
                    context.startedAt(),
                    Instant.now(),
                    java.util.List.of(),
                    Map.of()
            ));
        }

        LOG.infof("Executing coordination with strategy: %s (pattern=%s)", strategy.id(), strategy.pattern());

        // 1. Generate Coordination Plan IR
        CoordinationPlan plan = strategy.plan(request);

        // 2. Compile Plan to Workflow Definition IR
        CoordinationPlanCompiler compiler = resolvePlanCompiler();
        CoordinationWorkflowDefinition workflowDef = compiler.compile(plan);

        // 3. Concurrency-safe Get or Create in Gamelan by Fingerprint
        CoordinationWorkflowDefinitionRegistry defRegistry = resolveDefinitionRegistry();
        GamelanClient client = resolveClient();

        if (defRegistry == null || client == null) {
            return Uni.createFrom().item(new CoordinationResult(
                    context.executionId().toString(),
                    CoordinationResult.Status.FAILED,
                    Map.of(),
                    "Missing Gamelan backend infrastructure (registry or client null)",
                    context.startedAt(),
                    Instant.now(),
                    plan.agentIds(),
                    Map.of()
            ));
        }

        String effectiveTenant = context.tenantId() != null ? context.tenantId() : "default";

        Map<String, Object> inputParameters = request.request() != null && request.request().parameters() != null
                ? request.request().parameters()
                : Map.of();

        return defRegistry.getOrCreate(workflowDef, effectiveTenant)
                .flatMap(definitionId -> {
                    LOG.debugf("Running Gamelan workflow definition: %s", definitionId);

                    return client.runs()
                            .create(definitionId)
                            .correlationId(context.executionId().toString())
                            .inputs(inputParameters)
                            .execute()
                            .flatMap(createdRun -> client.runs().start(createdRun.getRunId()))
                            .map(startedRun -> toCoordinationResult(startedRun, plan, context));
                })
                .onFailure().recoverWithItem(err -> {
                    LOG.errorf(err, "Coordination execution failed for executionId: %s", context.executionId());
                    return new CoordinationResult(
                            context.executionId().toString(),
                            CoordinationResult.Status.FAILED,
                            Map.of(),
                            err.getMessage(),
                            context.startedAt(),
                            Instant.now(),
                            plan.agentIds(),
                            Map.of()
                    );
                });
    }

    private CoordinationResult toCoordinationResult(RunResponse run, CoordinationPlan plan, CoordinationContext context) {
        Map<String, Object> outputs = run.getOutputs() != null ? run.getOutputs() : Map.of();
        return new CoordinationResult(
                context.executionId().toString(),
                CoordinationResult.Status.COMPLETED,
                outputs,
                null,
                context.startedAt(),
                Instant.now(),
                plan.agentIds(),
                Map.of("gamelan.runId", run.getRunId() != null ? run.getRunId() : "")
        );
    }

    private CoordinationRegistry resolveCoordinationRegistry() {
        if (coordinationRegistry != null) {
            return coordinationRegistry;
        }
        if (coordinationRegistryInstances != null && coordinationRegistryInstances.isResolvable()) {
            return coordinationRegistryInstances.get();
        }
        return null;
    }

    private CoordinationPlanCompiler resolvePlanCompiler() {
        if (planCompiler != null) {
            return planCompiler;
        }
        if (planCompilerInstances != null && planCompilerInstances.isResolvable()) {
            return planCompilerInstances.get();
        }
        return new DefaultCoordinationPlanCompiler();
    }

    private CoordinationWorkflowDefinitionRegistry resolveDefinitionRegistry() {
        if (definitionRegistry != null) {
            return definitionRegistry;
        }
        if (definitionRegistryInstances != null && definitionRegistryInstances.isResolvable()) {
            return definitionRegistryInstances.get();
        }
        return null;
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
