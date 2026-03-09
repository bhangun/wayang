package tech.kayys.wayang.agent.skill;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.prompt.core.RenderingEngineRegistry;
import tech.kayys.wayang.prompt.core.PromptVariableDefinition;
import tech.kayys.wayang.prompt.core.PromptVariableValue;
import tech.kayys.wayang.prompt.core.PromptVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders system and user prompts from a {@link SkillDefinition} and task
 * context.
 *
 * <p>
 * Integrates with the existing Wayang Prompt Module ({@code wayang-prompt})
 * for template rendering, leveraging its {@link RenderingEngineRegistry} which
 * supports Simple ({{variable}}), Jinja2, and FreeMarker strategies.
 *
 * <p>
 * Handles:
 * <ul>
 * <li>Sub-skill selection (e.g. taskType=REVIEW picks the REVIEW sub-skill
 * prompt)</li>
 * <li>User prompt template rendering via the platform's rendering engines</li>
 * <li>Default user prompt construction when no template is specified</li>
 * </ul>
 */
@ApplicationScoped
public class SkillPromptRenderer {

    private static final Logger log = LoggerFactory.getLogger(SkillPromptRenderer.class);

    @Inject
    RenderingEngineRegistry renderingEngineRegistry;

    /**
     * Render the system prompt for the given skill and task type.
     */
    public String renderSystemPrompt(SkillDefinition skill, String taskType) {
        return skill.effectiveSystemPrompt(taskType);
    }

    /**
     * Render the user prompt from the skill's template and task context.
     */
    public String renderUserPrompt(SkillDefinition skill, String instruction, Map<String, Object> context) {
        String template = skill.userPromptTemplate();

        if (template != null && !template.isBlank()) {
            return renderViaPromptEngine(template, instruction, context);
        }

        return buildDefaultUserPrompt(instruction, context);
    }

    /**
     * Render a template using the Wayang Prompt Module's Simple rendering engine.
     */
    private String renderViaPromptEngine(String template, String instruction, Map<String, Object> context) {
        try {
            long now = System.currentTimeMillis();
            List<PromptVariableValue> variables = new ArrayList<>();
            variables.add(new PromptVariableValue(
                    "instruction",
                    instruction != null ? instruction : "",
                    PromptVariableDefinition.VariableSource.INPUT,
                    false,
                    now));

            if (context != null) {
                for (Map.Entry<String, Object> entry : context.entrySet()) {
                    if (entry.getValue() != null && !isInternalField(entry.getKey())) {
                        variables.add(new PromptVariableValue(
                                entry.getKey(),
                                entry.getValue(),
                                PromptVariableDefinition.VariableSource.CONTEXT,
                                false,
                                now));
                    }
                }
                variables.add(new PromptVariableValue(
                        "context",
                        formatContext(context),
                        PromptVariableDefinition.VariableSource.CONTEXT,
                        false,
                        now));
            }

            var engine = renderingEngineRegistry.forStrategy(PromptVersion.RenderingStrategy.SIMPLE);
            return engine.expand(template, variables);

        } catch (Exception e) {
            log.warn("Failed to render skill prompt template via PromptEngine, falling back to default: {}",
                    e.getMessage());
            return buildDefaultUserPrompt(instruction, context);
        }
    }

    private String buildDefaultUserPrompt(String instruction, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();

        if (instruction != null && !instruction.isBlank()) {
            prompt.append("Task: ").append(instruction).append("\n\n");
        }

        if (context != null && !context.isEmpty()) {
            appendIfPresent(prompt, "Language", context.get("language"));
            appendIfPresent(prompt, "Framework", context.get("framework"));

            Object code = context.get("code");
            if (code != null) {
                prompt.append("Existing Code:\n```\n").append(code).append("\n```\n\n");
            }

            appendIfPresent(prompt, "Requirements", context.get("requirements"));
            appendIfPresent(prompt, "Data", context.get("data"));
            appendIfPresent(prompt, "Input Data", context.get("inputData"));

            StringBuilder otherContext = new StringBuilder();
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String key = entry.getKey();
                if (isInternalField(key) || isWellKnownField(key) || entry.getValue() == null) {
                    continue;
                }
                otherContext.append("- ").append(key).append(": ").append(entry.getValue()).append("\n");
            }
            if (!otherContext.isEmpty()) {
                prompt.append("Context:\n").append(otherContext).append("\n");
            }
        }

        return prompt.toString();
    }

    private String formatContext(Map<String, Object> context) {
        if (context == null || context.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        context.forEach((key, value) -> {
            if (!isInternalField(key) && value != null) {
                sb.append("- ").append(key).append(": ").append(value).append("\n");
            }
        });
        return sb.toString();
    }

    private void appendIfPresent(StringBuilder sb, String label, Object value) {
        if (value != null) {
            sb.append(label).append(": ").append(value).append("\n\n");
        }
    }

    private boolean isInternalField(String key) {
        return key.startsWith("_") || key.equals("agentType") || key.equals("skillId")
                || key.equals("taskType") || key.equals("preferredProvider")
                || key.equals("fallbackProvider") || key.equals("temperature")
                || key.equals("maxTokens") || key.equals("agentId");
    }

    private boolean isWellKnownField(String key) {
        return key.equals("language") || key.equals("framework") || key.equals("code")
                || key.equals("requirements") || key.equals("data") || key.equals("inputData");
    }
}
