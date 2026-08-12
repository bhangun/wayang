package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.execution.cache.ExecutionCache;
import tech.kayys.wayang.provider.Provider;

/**
 * Service for creating and managing {@link AgentExecution} instances.
 *
 * <p>Wires in CDI-managed {@link Provider} and {@link ExecutionCache} instances,
 * passing them down to {@link DefaultAgentExecution} so the real ReAct loop can
 * connect to the LLM provider and benefit from execution-scoped caching.</p>
 */
@ApplicationScoped
public class AgentExecutionService {

    @Inject
    CheckpointStore checkpointStore;

    @Inject
    AgentToolExecutor toolExecutor;

    /**
     * All CDI-managed {@link Provider} beans discovered at startup.
     */
    @Inject
    Instance<Provider> providerInstances;

    /**
     * Optional execution cache — available when {@code InMemoryExecutionCache}
     * (or a distributed alternative) is on the classpath.
     */
    @Inject
    Instance<ExecutionCache> executionCacheInstances;

    // ------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------

    public AgentExecution create(AgentDefinition agent, AgentRequest request, ExecutionBudget budget) {
        String executionId = java.util.UUID.randomUUID().toString();

        AgentContext agentContext = AgentContext.builder()
            .id(new tech.kayys.wayang.identity.ResourceId.AgentId(
                    new tech.kayys.wayang.extension.Id(java.util.UUID.randomUUID())))
            .request(request)
            .build();

        List<Provider> providers = new ArrayList<>();
        if (providerInstances != null) {
            providerInstances.forEach(providers::add);
        }

        ExecutionCache cache = resolveCache();
        ExecutionBudget effectiveBudget = budget != null ? budget : ExecutionBudget.balanced();

        return new DefaultAgentExecution(
            executionId,
            agent,
            agentContext,
            effectiveBudget,
            checkpointStore,
            toolExecutor,
            providers,
            null, // ModelRouter — resolved inside DefaultAgentExecution
            null, // ContextPlanner — resolved inside DefaultAgentExecution
            null, // MemoryManager — resolved inside DefaultAgentExecution
            cache,
            null, // tenantId — null in standalone mode
            null  // userId   — null in standalone mode
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
            null, null, null,
            resolveCache(),
            null, null
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
    // Internal
    // ------------------------------------------------------------------

    private ExecutionCache resolveCache() {
        if (executionCacheInstances == null || executionCacheInstances.isUnsatisfied()) {
            return null;
        }
        return executionCacheInstances.get();
    }
}
