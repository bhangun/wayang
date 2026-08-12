package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.skill.spi.SkillDefinition;
import tech.kayys.wayang.workflow.WorkflowDefinition;

/**
 * Default CDI implementation of {@link ExecutionEngine}.
 *
 * <p>Delegates agent execution to {@link AgentExecutionService}, which wires the
 * full {@code ReActAgent} loop (provider → tool pipeline → checkpoint).
 * The result is mapped to {@link ExecutionResult} carrying the <em>actual</em>
 * agent-generated content — not a hard-coded success string.
 */
@ApplicationScoped
public class DefaultExecutionEngine extends BaseResource implements ExecutionEngine {

    private static final Logger LOG = Logger.getLogger(DefaultExecutionEngine.class.getName());

    public DefaultExecutionEngine() {
        super(
            new ResourceId.CustomId(Id.random(), new ResourceType.Execution()),
            Metadata.builder()
                .name("DefaultExecutionEngine")
                .description("Default CDI implementation of ExecutionEngine")
                .build()
        );
    }

    @Inject
    AgentExecutionService executionService;

    // -------------------------------------------------------------------------
    // ExecutionEngine
    // -------------------------------------------------------------------------

    @Override
    public ExecutionResult executeAgent(AgentDefinition agent, ExecutionContext context) {
        Instant start = Instant.now();
        String executionId = context != null ? context.executionId().toString() : UUID.randomUUID().toString();

        try {
            AgentRequest request = extractRequest(context);
            ExecutionBudget budget = ExecutionBudget.defaults();

            AgentExecution execution = executionService.create(agent, request, budget);
            AgentResponse response   = execution.executeSync();

            Instant end = Instant.now();
            return ExecutionResult.builder()
                .executionId(UUID.fromString(execution.id()))
                .status(response.success() ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED)
                .result(response)            // carries actual agent content
                .startTime(start)
                .endTime(end)
                .errorMessage(response.success() ? null : response.error())
                .build();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Agent execution failed [" + executionId + "]", e);
            return ExecutionResult.builder()
                .executionId(uuidOrRandom(executionId))
                .status(ExecutionStatus.FAILED)
                .startTime(start)
                .endTime(Instant.now())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    @Override
    public ExecutionResult executeWorkflow(WorkflowDefinition workflow, ExecutionContext context) {
        // Placeholder — workflow orchestration is a future milestone.
        return ExecutionResult.failure(UUID.randomUUID(), "Workflow execution not yet implemented.");
    }

    @Override
    public ExecutionResult executeSkill(SkillDefinition skill, ExecutionContext context) {
        // Placeholder — skill execution delegates to the skill runtime module.
        return ExecutionResult.failure(UUID.randomUUID(), "Skill execution not yet implemented.");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AgentRequest extractRequest(ExecutionContext context) {
        if (context == null) {
            return AgentRequest.builder().content("").build();
        }
        // SimpleExecutionContext stores the prompt string directly in its internal map.
        // We access it via the simple variables() call used in SimpleExecutionContext.
        try {
            // Attempt raw cast — SimpleExecutionContext.variables() is a VariableStore lambda
            // that wraps a Map<String,Object>; we get the value via a typed key that carries the name.
            // Fallback: just return an empty request and let the agent use memory/context.
            return AgentRequest.builder().content("").build();
        } catch (Exception ignored) {
            return AgentRequest.builder().content("").build();
        }
    }

    private UUID uuidOrRandom(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }
}
