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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unified skill-based agent executor that replaces all individual agent
 * executor classes.
 *
 * <p>
 * Instead of having separate {@code CoderAgentExecutor},
 * {@code PlannerAgentExecutor},
 * {@code AnalyticAgentExecutor}, etc., this single executor handles ALL agent
 * types by
 * reading the skill definition from the {@link SkillRegistry} at runtime.
 *
 * <p>
 * The executor reads the {@code skillId} from the task context, looks up the
 * corresponding {@link SkillDefinition}, renders prompts via
 * {@link SkillPromptRenderer},
 * and calls {@link GollekInferenceService} for inference.
 *
 * <p>
 * On the UI canvas, users simply drag an "Agent" node, assign a skill (built-in
 * or
 * custom), and the executor handles the rest — no new Java code needed.
 *
 * <h3>Task Context Properties</h3>
 * <ul>
 * <li>{@code skillId} — (required) the skill to use, e.g. "coder", "planner",
 * "my-custom-skill"</li>
 * <li>{@code taskType} — (optional) sub-skill selection, e.g. "GENERATE",
 * "REVIEW"</li>
 * <li>{@code instruction} — (required) the user's instruction/goal</li>
 * <li>{@code preferredProvider} — (optional) override the skill's default
 * provider</li>
 * <li>{@code temperature} — (optional) override the skill's default
 * temperature</li>
 * <li>{@code maxTokens} — (optional) override the skill's default max
 * tokens</li>
 * </ul>
 */
@Executor(executorType = "agent", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 20)
@ApplicationScoped
public class SkillBasedAgentExecutor extends AbstractAgentExecutor {

    private static final Logger log = LoggerFactory.getLogger(SkillBasedAgentExecutor.class);

    @Inject
    GollekInferenceService inferenceService;

    @Inject
    SkillRegistry skillRegistry;

    @Inject
    SkillPromptRenderer promptRenderer;

    @Inject
    tech.kayys.wayang.agent.core.tool.ToolRegistry toolRegistry;

    @Override
    public String getExecutorType() {
        return "agent";
    }

    @Override
    protected AgentType getAgentType() {
        return AgentType.SPECIALIST; // Generic — actual type comes from the skill
    }

    /**
     * This executor handles tasks with either "agent" agentType or any registered
     * skillId.
     */
    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();

        // Handle the unified "agent" type
        String agentType = (String) context.get("agentType");
        if ("agent".equals(agentType)) {
            return true;
        }

        // Also handle if a skillId is specified and matches a registered skill
        String skillId = (String) context.get("skillId");
        if (skillId != null && skillRegistry.hasSkill(skillId)) {
            return true;
        }

        // Backward compatibility: handle legacy agent type names mapped to skills
        if (agentType != null) {
            String mappedSkillId = mapLegacyAgentType(agentType);
            return mappedSkillId != null && skillRegistry.hasSkill(mappedSkillId);
        }

        return false;
    }

    @Override
    protected Uni<NodeExecutionResult> doExecute(NodeExecutionTask task) {
        log.info("SkillBasedAgentExecutor executing task: {}", task.nodeId());

        return Uni.createFrom().item(() -> {
            try {
                Map<String, Object> context = task.context();

                // 1. Resolve the skill
                SkillDefinition skill = resolveSkill(context);
                if (skill == null) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "No skill found. Specify 'skillId' in task context."));
                }

                // 2. If this is an orchestrator skill, reject — use OrchestratorSkillExecutor
                if (skill.isOrchestrator()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "Orchestrator skills must use the 'agent-orchestrator' executor type."));
                }

                // 3. Extract instruction
                String instruction = resolveInstruction(context);
                if (instruction == null || instruction.isBlank()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.VALIDATION_FAILED,
                            "Instruction is required. Provide 'instruction', 'goal', or 'objective' in task context."));
                }

                // 4. Resolve task type for sub-skill selection
                String taskType = (String) context.get("taskType");

                // 5. Render prompts using the skill definition
                String systemPrompt = promptRenderer.renderSystemPrompt(skill, taskType);
                String userPrompt = promptRenderer.renderUserPrompt(skill, instruction, context);

                // 6. Resolve inference parameters (task context overrides skill defaults)
                String provider = resolveProvider(context, skill);
                String fallback = resolveFallback(context, skill);
                double temperature = resolveTemperature(context, skill);
                int maxTokens = resolveMaxTokens(context, skill);

                // 6.5. Resolve skill-defined tools from ToolRegistry
                java.util.List<tech.kayys.gollek.spi.tool.ToolDefinition> toolDefinitions = null;
                if (skill.tools() != null && !skill.tools().isEmpty()) {
                    toolDefinitions = skill.tools().stream()
                            .map(toolId -> toolRegistry.getTool(toolId))
                            .filter(java.util.Optional::isPresent)
                            .map(java.util.Optional::get)
                            .map(tool -> tech.kayys.gollek.spi.tool.ToolDefinition.builder()
                                    .name(tool.id())
                                    .description(tool.description())
                                    .parameters(tool.inputSchema())
                                    .build())
                            .toList();
                }

                // 7. Check if streaming is requested
                boolean streaming = Boolean.TRUE.equals(context.get("stream"));

                // 8. Build inference request
                Map<String, Object> additionalParams = new LinkedHashMap<>();
                additionalParams.put("context", context);
                additionalParams.put("skillId", skill.id());
                if (taskType != null) {
                    additionalParams.put("taskType", taskType);
                }

                AgentInferenceRequest request = AgentInferenceRequest.builder()
                        .systemPrompt(systemPrompt)
                        .userPrompt(userPrompt)
                        .preferredProvider(provider)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .tools(toolDefinitions)
                        .additionalParams(additionalParams)
                        .agentId(context.containsKey("agentId") ? (String) context.get("agentId") : task.nodeId().value())
                        .useMemory(true)
                        .stream(streaming)
                        .build();

                // 9. Execute inference
                //    - If tools are present → use ReAct tool loop
                //    - If streaming → use streaming path
                //    - Otherwise → simple inference with fallback
                AgentInferenceResponse response;
                if (streaming) {
                    StringBuilder sb = new StringBuilder();
                    inferenceService.inferStream(request)
                            .subscribe().with(
                                    chunk -> sb.append(chunk.getDelta() != null ? chunk.getDelta() : ""),
                                    err -> log.error("Stream error", err));
                    response = AgentInferenceResponse.builder()
                            .content(sb.toString())
                            .build();
                } else if (toolDefinitions != null && !toolDefinitions.isEmpty()) {
                    // ReAct tool execution loop
                    response = inferenceService.inferWithToolLoop(request);
                } else {
                    response = inferenceService.inferWithFallback(request, fallback);
                }

                if (response.isError()) {
                    return createFailureResult(task, new WayangException(
                            ErrorCode.INFERENCE_REQUEST_FAILED,
                            response.getError()));
                }

                // 10. Build result
                Map<String, Object> output = new LinkedHashMap<>();
                output.put("status", "COMPLETED");
                output.put("result", response.getContent());
                output.put("skillId", skill.id());
                output.put("skillName", skill.name());
                if (taskType != null)
                    output.put("taskType", taskType);
                output.put("provider", response.getProviderUsed());
                output.put("model", response.getModelUsed());
                output.put("tokens", response.getTotalTokens() != null ? response.getTotalTokens() : 0);
                if (response.getLatency() != null)
                    output.put("latency_ms", response.getLatency().toMillis());

                // Include tool execution audit trail
                if (response.getToolResults() != null && !response.getToolResults().isEmpty()) {
                    output.put("toolExecutions", response.getToolResults().size());
                    output.put("reactIterations", response.getIterations());
                }
                if (response.getFinishReason() != null) {
                    output.put("finishReason", response.getFinishReason());
                }

                return createSuccessResult(task, output);

            } catch (Exception e) {
                log.error("Skill-based agent execution failed", e);
                return createFailureResult(task, e);
            }
        });
    }

    // =========================================================================
    // Resolution helpers
    // =========================================================================

    /**
     * Resolve the skill from task context. Tries skillId first, then agentType
     * mapping.
     */
    private SkillDefinition resolveSkill(Map<String, Object> context) {
        // Direct skillId lookup
        String skillId = (String) context.get("skillId");
        if (skillId != null && !skillId.isBlank()) {
            return skillRegistry.getSkill(skillId).orElse(null);
        }

        // Legacy agentType mapping
        String agentType = (String) context.get("agentType");
        if (agentType != null) {
            String mapped = mapLegacyAgentType(agentType);
            if (mapped != null) {
                return skillRegistry.getSkill(mapped).orElse(null);
            }
        }

        // Default to "common" skill
        return skillRegistry.getSkill("common").orElse(null);
    }

    /**
     * Map legacy executor type names to skill IDs for backward compatibility.
     */
    private String mapLegacyAgentType(String agentType) {
        return switch (agentType) {
            case "agent-coder", "coder-agent" -> "coder";
            case "agent-planner", "planner-agent" -> "planner";
            case "analytics-agent", "analytic-agent" -> "analytics";
            case "evaluator-agent" -> "evaluator";
            case "common-agent" -> "common";
            case "agent" -> null; // Will use skillId or default
            default -> agentType; // Try as-is (might be a custom skill ID)
        };
    }

    private String resolveInstruction(Map<String, Object> context) {
        for (String key : new String[] { "instruction", "goal", "objective", "prompt", "query" }) {
            Object val = context.get(key);
            if (val instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private String resolveProvider(Map<String, Object> context, SkillDefinition skill) {
        String override = (String) context.get("preferredProvider");
        return (override != null && !override.isBlank()) ? override : skill.defaultProvider();
    }

    private String resolveFallback(Map<String, Object> context, SkillDefinition skill) {
        String override = (String) context.get("fallbackProvider");
        return (override != null && !override.isBlank()) ? override : skill.fallbackProvider();
    }

    private double resolveTemperature(Map<String, Object> context, SkillDefinition skill) {
        Object override = context.get("temperature");
        if (override instanceof Number n)
            return n.doubleValue();
        return skill.temperature() != null ? skill.temperature() : 0.7;
    }

    private int resolveMaxTokens(Map<String, Object> context, SkillDefinition skill) {
        Object override = context.get("maxTokens");
        if (override instanceof Number n)
            return n.intValue();
        return skill.maxTokens() != null ? skill.maxTokens() : 2048;
    }
}
