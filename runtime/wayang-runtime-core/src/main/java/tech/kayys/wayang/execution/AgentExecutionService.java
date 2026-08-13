package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.execution.cache.ExecutionCache;
import tech.kayys.wayang.execution.context.DefaultRuntimeContextPlanner;
import tech.kayys.wayang.execution.context.RuntimeContextPlanner;
import tech.kayys.wayang.memory.manager.MemoryManager;
import tech.kayys.wayang.execution.event.EventLedger;
import tech.kayys.wayang.provider.DefaultModelRouter;
import tech.kayys.wayang.provider.ModelRouter;
import tech.kayys.wayang.provider.Provider;

/**
 * Service for creating and managing {@link AgentExecution} instances.
 *
 * <p>Phase 4: Wires in CDI-managed {@link Provider}, {@link ModelRouter},
 * {@link RuntimeContextPlanner}, {@link MemoryManager}, and {@link ExecutionCache}
 * instances, passing them into {@link DefaultAgentExecution} so the kernel is
 * fully dependency-injected.</p>
 */
@ApplicationScoped
public class AgentExecutionService {

    @Inject
    CheckpointStore checkpointStore;

    @Inject
    AgentToolExecutor toolExecutor;

    /** All CDI-managed {@link Provider} beans discovered at startup. */
    @Inject
    Instance<Provider> providerInstances;

    /** Optional execution cache. */
    @Inject
    Instance<ExecutionCache> executionCacheInstances;

    /**
     * Optional model router — CDI-discovered; falls back to {@link DefaultModelRouter}
     * if none is configured.
     */
    @Inject
    Instance<ModelRouter> modelRouterInstances;

    /**
     * Optional runtime context planner — CDI-discovered; falls back to
     * {@link DefaultRuntimeContextPlanner} if none is configured.
     */
    @Inject
    Instance<RuntimeContextPlanner> contextPlannerInstances;

    /**
     * Optional memory manager — CDI-discovered; may be absent in minimal deployments.
     */
    @Inject
    Instance<MemoryManager> memoryManagerInstances;

    /**
     * All CDI-managed {@link ContextProvider} beans for context assembly.
     */
    @Inject
    Instance<ContextProvider> contextProviderInstances;

    /**
     * Phase 5: Optional Event Ledger — CDI-discovered; absent in test/minimal deployments.
     */
    @Inject
    Instance<EventLedger> eventLedgerInstances;

    // ------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------

    public AgentExecution create(AgentDefinition agent, AgentRequest request, ExecutionBudget budget) {
        ExecutionIdentity identity = ExecutionIdentity.create();

        AgentContext agentContext = AgentContext.builder()
            .id(new tech.kayys.wayang.identity.ResourceId.ExecutionId(
                    new tech.kayys.wayang.extension.Id(java.util.UUID.fromString(identity.executionId()))))
            .request(request)
            .build();

        List<Provider> providers = new ArrayList<>();
        if (providerInstances != null) {
            providerInstances.forEach(providers::add);
        }

        ExecutionCache cache = resolveCache();
        ExecutionBudget effectiveBudget = budget != null ? budget : ExecutionBudget.balanced();

        return new DefaultAgentExecution(
            identity.executionId(),
            agent,
            agentContext,
            effectiveBudget,
            checkpointStore,
            toolExecutor,
            providers,
            resolveModelRouter(),
            resolveContextPlanner(),
            resolveMemoryManager(),
            cache,
            null, // tenantId — null in standalone mode
            null, // userId   — null in standalone mode
            resolveEventLedger()
        );
    }

    /** Convenience overload using the {@code balanced} profile. */
    public AgentExecution create(AgentDefinition agent, AgentRequest request) {
        return create(agent, request, ExecutionBudget.balanced());
    }

    // ------------------------------------------------------------------
    // Resume / approval
    // ------------------------------------------------------------------

    public AgentExecution resume(String executionId) {
        java.util.Optional<AgentContext> contextOpt = checkpointStore.load(executionId);
        if (contextOpt.isEmpty()) {
            throw new IllegalArgumentException("Execution not found: " + executionId);
        }

        DefaultAgentExecution execution = new DefaultAgentExecution(
            executionId,
            null,
            contextOpt.get(),
            ExecutionBudget.balanced(),
            checkpointStore,
            toolExecutor,
            List.of(),
            resolveModelRouter(),
            resolveContextPlanner(),
            resolveMemoryManager(),
            resolveCache(),
            null, null,
            resolveEventLedger()
        );
        execution.resume();
        return execution;
    }

    public void approve(String executionId, AgentDecision decision) {
        AgentExecution execution = resume(executionId);
        if (decision instanceof AgentDecision.WaitForApproval) {
            execution.execute();
        }
    }

    // ------------------------------------------------------------------
    // Internal resolver helpers
    // ------------------------------------------------------------------

    private ExecutionCache resolveCache() {
        if (executionCacheInstances == null || executionCacheInstances.isUnsatisfied()) {
            return null;
        }
        return executionCacheInstances.get();
    }

    private ModelRouter resolveModelRouter() {
        if (modelRouterInstances == null || modelRouterInstances.isUnsatisfied()) {
            return new DefaultModelRouter();
        }
        return modelRouterInstances.get();
    }

    private RuntimeContextPlanner resolveContextPlanner() {
        if (contextPlannerInstances == null || contextPlannerInstances.isUnsatisfied()) {
            return new DefaultRuntimeContextPlanner();
        }
        return contextPlannerInstances.get();
    }

    private MemoryManager resolveMemoryManager() {
        if (memoryManagerInstances == null || memoryManagerInstances.isUnsatisfied()) {
            return null;
        }
        return memoryManagerInstances.get();
    }

    private EventLedger resolveEventLedger() {
        if (eventLedgerInstances == null || eventLedgerInstances.isUnsatisfied()) {
            return null;
        }
        return eventLedgerInstances.get();
    }
}
