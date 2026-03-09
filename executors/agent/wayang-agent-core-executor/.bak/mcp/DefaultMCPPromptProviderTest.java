package tech.kayys.wayang.agent.mcp.impl;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.mcp.model.MCPPrompt;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultMCPPromptProviderTest {

    @Test
    void renderPromptSubstitutesTemplateVariables() {
        DefaultMCPPromptProvider provider = new DefaultMCPPromptProvider();
        MCPPrompt prompt = MCPPrompt.builder()
                .name("planner")
                .description("Planner prompt")
                .arguments(List.of(new MCPPrompt.PromptArgument("topic", "Topic", true)))
                .template("Plan for {{ topic }}")
                .build();

        provider.registerPrompt(prompt).await().indefinitely();

        String rendered = provider.renderPrompt("planner", Map.of("topic", "agent orchestration"))
                .await().indefinitely();

        assertEquals("Plan for agent orchestration", rendered);
    }

    @Test
    void renderPromptFailsWhenRequiredArgumentMissing() {
        DefaultMCPPromptProvider provider = new DefaultMCPPromptProvider();
        MCPPrompt prompt = MCPPrompt.builder()
                .name("evaluator")
                .description("Evaluator prompt")
                .arguments(List.of(new MCPPrompt.PromptArgument("result", "Result", true)))
                .template("Evaluate {{ result }}")
                .build();

        provider.registerPrompt(prompt).await().indefinitely();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> provider.renderPrompt("evaluator", Map.of()).await().indefinitely());

        assertEquals("Missing required argument: result", error.getMessage());
    }
}
