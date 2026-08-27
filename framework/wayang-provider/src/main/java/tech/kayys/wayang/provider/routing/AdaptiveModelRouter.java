package tech.kayys.wayang.provider.routing;

import org.jboss.logging.Logger;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.ContextRequirements;
import tech.kayys.wayang.provider.ModelRouter;
import tech.kayys.wayang.provider.Provider;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptive / Advanced ModelRouter implementation.
 * Acts as an orchestrator across decomposed sub-components:
 * 1. ModelRegistry & ModelDeploymentRegistry (Candidate Discovery)
 * 2. ConstraintEvaluator (Hard Constraint & Policy Filtering)
 * 3. ModelScorer (Soft Preference Multi-Criteria Scoring)
 * 4. ModelRoutingTelemetry (Runtime Health & Latency Multipliers)
 * 5. FallbackPlanner (Multi-strategy Fallback Target Assembly)
 */
public class AdaptiveModelRouter implements ModelRouter {

    private static final Logger LOG = Logger.getLogger(AdaptiveModelRouter.class);

    private final ModelRegistry modelRegistry;
    private final ModelDeploymentRegistry deploymentRegistry;
    private final ConstraintEvaluator constraintEvaluator;
    private final ModelScorer modelScorer;
    private final ModelRoutingTelemetry telemetry;
    private final FallbackPlanner fallbackPlanner;

    public AdaptiveModelRouter() {
        this(new DefaultModelRegistry(),
             new DefaultModelDeploymentRegistry(),
             new ConstraintEvaluator(),
             new DefaultModelScorer(),
             ModelRoutingTelemetry.getInstance(),
             new FallbackPlanner());
    }

    public AdaptiveModelRouter(ModelRegistry modelRegistry, ModelScorer modelScorer, ModelRoutingTelemetry telemetry) {
        this(modelRegistry,
             new DefaultModelDeploymentRegistry(),
             new ConstraintEvaluator(),
             modelScorer,
             telemetry,
             new FallbackPlanner());
    }

    public AdaptiveModelRouter(
            ModelRegistry modelRegistry,
            ModelDeploymentRegistry deploymentRegistry,
            ConstraintEvaluator constraintEvaluator,
            ModelScorer modelScorer,
            ModelRoutingTelemetry telemetry,
            FallbackPlanner fallbackPlanner
    ) {
        this.modelRegistry = modelRegistry != null ? modelRegistry : new DefaultModelRegistry();
        this.deploymentRegistry = deploymentRegistry != null ? deploymentRegistry : new DefaultModelDeploymentRegistry();
        this.constraintEvaluator = constraintEvaluator != null ? constraintEvaluator : new ConstraintEvaluator();
        this.modelScorer = modelScorer != null ? modelScorer : new DefaultModelScorer();
        this.telemetry = telemetry != null ? telemetry : ModelRoutingTelemetry.getInstance();
        this.fallbackPlanner = fallbackPlanner != null ? fallbackPlanner : new FallbackPlanner();
    }

    @Override
    public InferencePlan plan(
            AgentRequest request,
            AgentDefinition agentDefinition,
            InferenceRequirements requirements,
            InferencePolicy policy,
            List<Provider> availableProviders
    ) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No available providers to route to");
        }

        String requestId = (request != null && request.id() != null) ? request.id() : "req-" + UUID.randomUUID();
        InferenceRequirements reqs = requirements != null ? requirements : InferenceRequirements.defaults();
        InferencePolicy pol = policy != null ? policy : InferencePolicy.defaults();

        // 1. Candidate Discovery
        List<ModelSpec> allModels = modelRegistry.listModels();
        Map<String, String> rejected = new LinkedHashMap<>();
        List<ModelSpec> candidateModels = new ArrayList<>();

        // 2. Hard Constraint Evaluation
        for (ModelSpec model : allModels) {
            ConstraintEvaluator.EvaluationResult eval = constraintEvaluator.evaluate(model, reqs, pol);
            if (eval.isAllowed()) {
                candidateModels.add(model);
            } else {
                rejected.put(model.modelId(), eval.rejectionReason());
            }
        }

        // If all candidates rejected, relax constraints to ensure continuity
        if (candidateModels.isEmpty()) {
            LOG.warnf("All models rejected for request %s. Relaxing constraints to standard catalog.", requestId);
            candidateModels.addAll(allModels);
        }

        // 3. Soft Preference Multi-Criteria Scoring
        List<InferencePlan.ScoredCandidate> scored = new ArrayList<>();
        for (ModelSpec candidate : candidateModels) {
            scored.add(modelScorer.score(candidate, reqs, pol, telemetry));
        }
        scored.sort((a, b) -> Double.compare(b.totalScore(), a.totalScore()));

        // 4. Primary Selection & Provider Resolution
        InferencePlan.ScoredCandidate topScored = scored.get(0);
        ModelSpec selectedModelSpec = topScored.modelSpec();
        Provider selectedProvider = resolveProvider(selectedModelSpec, availableProviders);

        long inputTokens = Math.max(reqs.requiredContextTokens(), 2048);
        double totalEstimatedCost = selectedModelSpec.estimateCost(inputTokens, 1024);

        // 5. Fallback Planning
        List<InferencePlan.FallbackTarget> fallbackTargets = fallbackPlanner.planFallbacks(
                topScored,
                scored,
                pol,
                availableProviders,
                this::resolveProvider
        );

        // 6. Decision Reason Assembly
        String decisionReason = String.format(
                "Selected '%s' via '%s' (score: %.3f | Q: %.2f, C: %.2f, L: %.2f, Health: %.2f) " +
                        "meeting input %s -> output %s [caps=%s, context=%d tokens, objective=%s]",
                selectedModelSpec.modelId(),
                selectedProvider.getClass().getSimpleName(),
                topScored.totalScore(),
                topScored.qualityComponent(),
                topScored.costComponent(),
                topScored.latencyComponent(),
                topScored.healthMultiplier(),
                reqs.inputModalities(),
                reqs.outputModalities(),
                reqs.requiredCapabilities(),
                reqs.requiredContextTokens(),
                pol.objective()
        );

        return new InferencePlan(
                requestId,
                selectedModelSpec.modelId(),
                selectedProvider,
                reqs,
                pol,
                scored,
                rejected,
                fallbackTargets,
                totalEstimatedCost,
                decisionReason,
                Instant.now()
        );
    }

    @Override
    public Provider route(AgentRequest request, AgentDefinition agentDefinition, List<Provider> availableProviders) {
        return plan(request, agentDefinition, InferenceRequirements.defaults(), InferencePolicy.defaults(), availableProviders).selectedProvider();
    }

    @Override
    public Provider route(ContextRequirements requirements, List<Provider> availableProviders) {
        InferenceRequirements reqs = InferenceRequirements.of(
                requirements.modalities(),
                requirements.requiresToolCalling(),
                requirements.requiredTokens()
        );
        return plan(null, null, reqs, InferencePolicy.defaults(), availableProviders).selectedProvider();
    }

    private Provider resolveProvider(ModelSpec modelSpec, List<Provider> availableProviders) {
        // Check active deployments in registry first
        List<ModelDeployment> deployments = deploymentRegistry.findAvailable(modelSpec.modelId());
        for (ModelDeployment dep : deployments) {
            if (dep.providerInstance() != null) {
                return dep.providerInstance();
            }
        }

        String providerId = modelSpec.providerId().toLowerCase();
        String modelId = modelSpec.modelId().toLowerCase();

        // Exact ID match
        for (Provider p : availableProviders) {
            if (p.id() != null && (p.id().equalsIgnoreCase(providerId) || p.id().equalsIgnoreCase(modelId))) {
                return p;
            }
        }

        // Class name substring match
        for (Provider p : availableProviders) {
            String className = p.getClass().getSimpleName().toLowerCase();
            if (className.contains(providerId) || className.contains(modelId)) {
                return p;
            }
        }

        // Fallback
        return availableProviders.get(0);
    }
}
