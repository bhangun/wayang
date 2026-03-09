package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.skill.SkillDefinition;
import tech.kayys.wayang.agent.skill.SkillPromptRenderer;
import tech.kayys.wayang.agent.skill.SkillRegistry;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;

import java.util.*;

/**
 * Orchestrator executor that coordinates multiple skill-based agents.
 *
 * <p>
 * Uses the same skill-based architecture as {@link SkillBasedAgentExecutor} but
 * adds
 * multi-agent coordination capabilities: delegating sub-tasks to other agents,
 * synthesizing results, and managing execution flow.
 *
 * <p>
 * Like any agent, the orchestrator's behavior is defined by its skill (default:
 * "orchestrator").
 * Its skill definition includes an {@link SkillDefinition.OrchestrationConfig}
 * that declares
 * default child skills, orchestration type, and coordination strategy.
 */
@Executor(executorType = "agent-orchestrator", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 5)
@ApplicationScoped
public class OrchestratorSkillExecutor extends AbstractAgentExecutor {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorSkillExecutor.class);

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    SkillRegistry skillRegistry;

    @Inject
    SkillPromptRenderer promptRenderer;

    @Inject
    SkillBasedAgentExecutor skillBasedExecutor;

    @Override
    public String getExecutorType() {
        return "agent-orchestrator";
    }

    @Override
    protected AgentType getAgentType() {
        return AgentType.ORCHESTRATOR;
    }

    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        String agentType = (String) context.get("agentType");
        if ("agent-orchestrator".equals(agentType) || "orchestrator-agent".equals(agentType)) {
            return true;
        }
        String skillId = (String) context.get("skillId");
        if (skillId != null) {
            return skillRegistry.getSkill(skillId)
                    .map(SkillDefinition::isOrchestrator)
                    .orElse(false);
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        log.info("OrchestratorSkillExecutor executing task: {}", task.nodeId());

        Map<String, Object> context = task.context();

        String skillId = (String) context.getOrDefault("skillId", "orchestrator");
        SkillDefinition orchestratorSkill = skillRegistry.getSkill(skillId).orElse(null);
        if (orchestratorSkill == null) {
            return Uni.createFrom().item(createFailureResult(task,
                    new WayangException(ErrorCode.VALIDATION_FAILED,
                            "Orchestrator skill not found: " + skillId)));
        }

        List<Map<String, Object>> agentTasks = (List<Map<String, Object>>) context.get("agentTasks");
        if (agentTasks != null && !agentTasks.isEmpty()) {
            return executeMultiAgentOrchestration(task, orchestratorSkill, agentTasks, context);
        }

        String objective = resolveObjective(context);
        if (objective != null && !objective.isBlank()) {
            return executeAIDrivenOrchestration(task, orchestratorSkill, objective, context);
        }

        return Uni.createFrom().item(createFailureResult(task,
                new WayangException(ErrorCode.VALIDATION_FAILED,
                        "Orchestrator requires either 'agentTasks' or 'objective' in task context.")));
    }

    private Uni<NodeExecutionResult> executeMultiAgentOrchestration(
            NodeExecutionTask parentTask,
            SkillDefinition orchestratorSkill,
            List<Map<String, Object>> agentTasks,
            Map<String, Object> context) {

        String orchestrationType = (String) context.getOrDefault("orchestrationType",
                orchestratorSkill.orchestration() != null
                        ? orchestratorSkill.orchestration().defaultType()
                        : "SEQUENTIAL");

        return switch (orchestrationType.toUpperCase()) {
            case "PARALLEL" -> executeParallel(parentTask, agentTasks);
            case "SEQUENTIAL" -> executeSequential(parentTask, agentTasks);
            default -> executeSequential(parentTask, agentTasks);
        };
    }

    private Uni<NodeExecutionResult> executeSequential(
            NodeExecutionTask parentTask,
            List<Map<String, Object>> agentTasks) {

        List<Map<String, Object>> results = new ArrayList<>();
        Uni<Void> chain = Uni.createFrom().voidItem();

        for (Map<String, Object> subTaskDef : agentTasks) {
            chain = chain.chain(() -> {
                NodeExecutionTask subTask = createSubTask(parentTask, subTaskDef, results);
                return skillBasedExecutor.execute(subTask)
                        .invoke(result -> {
                            Map<String, Object> resultEntry = new LinkedHashMap<>();
                            resultEntry.put("skillId", subTaskDef.getOrDefault("skillId",
                                    subTaskDef.get("agentType")));
                            resultEntry.put("status", result.status().name());
                            resultEntry.put("output", result.output());
                            results.add(resultEntry);
                        })
                        .replaceWithVoid();
            });
        }

        return chain.map(ignored -> {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("status", "COMPLETED");
            output.put("orchestrationType", "SEQUENTIAL");
            output.put("agentResults", results);
            output.put("totalAgents", results.size());
            return createSuccessResult(parentTask, output);
        }).onFailure().recoverWithItem(error -> createFailureResult(parentTask, error));
    }

    private Uni<NodeExecutionResult> executeParallel(
            NodeExecutionTask parentTask,
            List<Map<String, Object>> agentTasks) {

        List<Uni<Map<String, Object>>> unis = new ArrayList<>();

        for (Map<String, Object> subTaskDef : agentTasks) {
            NodeExecutionTask subTask = createSubTask(parentTask, subTaskDef, List.of());
            Uni<Map<String, Object>> subUni = skillBasedExecutor.execute(subTask)
                    .map(result -> {
                        Map<String, Object> resultEntry = new LinkedHashMap<>();
                        resultEntry.put("skillId", subTaskDef.getOrDefault("skillId",
                                subTaskDef.get("agentType")));
                        resultEntry.put("status", result.status().name());
                        resultEntry.put("output", result.output());
                        return resultEntry;
                    });
            unis.add(subUni);
        }

        return Uni.join().all(unis).andCollectFailures()
                .map(results -> {
                    Map<String, Object> output = new LinkedHashMap<>();
                    output.put("status", "COMPLETED");
                    output.put("orchestrationType", "PARALLEL");
                    output.put("agentResults", results);
                    output.put("totalAgents", results.size());
                    return createSuccessResult(parentTask, output);
                })
                .onFailure().recoverWithItem(error -> createFailureResult(parentTask, error));
    }

    private Uni<NodeExecutionResult> executeAIDrivenOrchestration(
            NodeExecutionTask task,
            SkillDefinition orchestratorSkill,
            String objective,
            Map<String, Object> context) {

        return Uni.createFrom().item(() -> {
            try {
                String taskType = (String) context.getOrDefault("taskType", "DELEGATE");
                String systemPrompt = promptRenderer.renderSystemPrompt(orchestratorSkill, taskType);
                String userPrompt = promptRenderer.renderUserPrompt(orchestratorSkill, objective, context);

                String provider = orchestratorSkill.defaultProvider();
                String fallback = orchestratorSkill.fallbackProvider();

                Map<String, Object> additionalParams = new LinkedHashMap<>();
                additionalParams.put("context", context);
                additionalParams.put("skillId", orchestratorSkill.id());

                List<String> availableSkills = skillRegistry.listSkills().stream()
                        .filter(s -> !s.isOrchestrator())
                        .map(s -> s.id() + ": " + s.description())
                        .toList();
                additionalParams.put("availableSkills", availableSkills);

                AgentInferenceRequest request = AgentInferenceRequest.builder()
                        .systemPrompt(systemPrompt)
                        .userPrompt(userPrompt)
                        .preferredProvider(provider)
                        .temperature(orchestratorSkill.temperature() != null ? orchestratorSkill.temperature() : 0.3)
                        .maxTokens(orchestratorSkill.maxTokens() != null ? orchestratorSkill.maxTokens() : 4096)
                        .additionalParams(additionalParams)
                        .agentId((String) context.getOrDefault("agentId", task.nodeId()))
                        .useMemory(true)
                        .build();

                AgentInferenceResponse response = inferenceService.inferWithFallback(request, fallback);

                if (response.isError()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.INFERENCE_REQUEST_FAILED, response.getError()));
                }

                Map<String, Object> output = new LinkedHashMap<>();
                output.put("status", "COMPLETED");
                output.put("delegationPlan", response.getContent());
                output.put("skillId", orchestratorSkill.id());
                output.put("objective", objective);
                output.put("provider", response.getProviderUsed());
                output.put("model", response.getModelUsed());
                output.put("tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0);
                output.put("latency_ms", response.getLatency().toMillis());

                return createSuccessResult(task, output);

            } catch (Exception e) {
                log.error("Orchestrator execution failed", e);
                return createFailureResult(task, e);
            }
        });
    }

    /**
     * Create a sub-task for delegation to another agent.
     * Uses the NodeExecutionTask record canonical constructor.
     */
    private NodeExecutionTask createSubTask(
            NodeExecutionTask parentTask,
            Map<String, Object> subTaskDef,
            List<Map<String, Object>> previousResults) {

        Map<String, Object> subContext = new LinkedHashMap<>(subTaskDef);

        if (subContext.containsKey("agentType") && !subContext.containsKey("skillId")) {
            subContext.put("skillId", subContext.get("agentType"));
        }

        subContext.put("agentType", "agent");

        if (!previousResults.isEmpty()) {
            subContext.put("_previousResults", previousResults);
        }

        // NodeExecutionTask is a record — use canonical constructor
        return new NodeExecutionTask(
                parentTask.runId(),
                parentTask.nodeId(),
                parentTask.attempt(),
                parentTask.token(),
                subContext,
                parentTask.retryPolicy());
    }

    private String resolveObjective(Map<String, Object> context) {
        for (String key : new String[] { "objective", "goal", "instruction", "prompt" }) {
            Object val = context.get(key);
            if (val instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }
}
