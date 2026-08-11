package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.provider.Provider;

/**
 * Service for creating and managing {@link AgentExecution} instances.
 *
 * <p>Wires in CDI-managed {@link Provider} instances and passes them down to
 * {@link DefaultAgentExecution} so the real ReAct loop can connect to the LLM
 * provider instead of returning a stub response.</p>
 */
@ApplicationScoped
public class AgentExecutionService {

    @Inject
    CheckpointStore checkpointStore;

    @Inject
    AgentToolExecutor toolExecutor;

    /**
     * All CDI-managed {@link Provider} beans discovered at startup.
     * Typically there is one (e.g. Anthropic, OpenAI, Gollek), but the list
     * allows multi-provider configurations.
     */
    @Inject
    Instance<Provider> providerInstances;

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

        // Collect all available Provider beans into a plain list so
        // DefaultAgentExecution can resolve them without CDI dependency.
        List<Provider> providers = new ArrayList<>();
        if (providerInstances != null) {
            providerInstances.forEach(providers::add);
        }

        return new DefaultAgentExecution(
            executionId,
            agent,
            agentContext,
            budget,
            checkpointStore,
            toolExecutor,
            providers
        );
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
            null,
            checkpointStore,
            toolExecutor
        );
        execution.resume();
        return execution;
    }

    public void approve(String executionId, AgentDecision decision) {
        AgentExecution execution = resume(executionId);
        if (decision instanceof AgentDecision.WaitForApproval) {
            // Re-run with the approval applied.
            execution.execute();
        }
    }
}
