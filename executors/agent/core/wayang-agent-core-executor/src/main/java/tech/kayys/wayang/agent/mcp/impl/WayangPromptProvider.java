package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.mcp.MCPPromptProvider;
import tech.kayys.wayang.agent.mcp.model.MCPPrompt;
import tech.kayys.wayang.prompt.core.*;
import tech.kayys.wayang.prompt.registry.PromptTemplateRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced MCP Prompt Provider that integrates with wayang-prompt engine.
 * Supports Jinja2, FreeMarker, and simple template rendering.
 */
@ApplicationScoped
public class WayangPromptProvider implements MCPPromptProvider {

    @Inject
    PromptEngine promptEngine;

    @Inject
    PromptTemplateRegistry templateRegistry;

    @Inject
    RenderingEngineRegistry renderingEngineRegistry;

    private final Map<String, MCPPrompt> mcpPrompts = new ConcurrentHashMap<>();

    @Override
    public Uni<List<MCPPrompt>> listPrompts() {
        // Combine MCP prompts with wayang-prompt templates
        return templateRegistry.listByTenant("default", 0, 100)
                .map(templates -> {
                    List<MCPPrompt> result = templates.stream()
                            .map(this::convertToMCPPrompt)
                            .collect(Collectors.toList());
                    result.addAll(mcpPrompts.values());
                    return result;
                });
    }

    @Override
    public Uni<MCPPrompt> getPrompt(String name) {
        // Check MCP prompts first
        if (mcpPrompts.containsKey(name)) {
            return Uni.createFrom().item(mcpPrompts.get(name));
        }

        // Try to get from wayang-prompt registry
        return templateRegistry.findById(name, "default")
                .map(this::convertToMCPPrompt);
    }

    @Override
    public Uni<String> renderPrompt(String name, Map<String, Object> arguments) {
        // Check if it's an MCP prompt
        if (mcpPrompts.containsKey(name)) {
            return Uni.createFrom().item(renderMCPPrompt(mcpPrompts.get(name), arguments));
        }

        // Use wayang-prompt engine
        return renderWayangPromptReactive(name, arguments);
    }

    @Override
    public Uni<Void> registerPrompt(MCPPrompt prompt) {
        mcpPrompts.put(prompt.getName(), prompt);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> unregisterPrompt(String name) {
        mcpPrompts.remove(name);
        return Uni.createFrom().voidItem();
    }

    private String renderMCPPrompt(MCPPrompt prompt, Map<String, Object> arguments) {
        for (MCPPrompt.PromptArgument arg : prompt.getArguments()) {
            if (arg.isRequired() && !arguments.containsKey(arg.getName())) {
                throw new IllegalArgumentException("Missing required argument: " + arg.getName());
            }
        }

        String result = prompt.getTemplate();
        if (result == null)
            return "";
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    private Uni<String> renderWayangPromptReactive(String name, Map<String, Object> arguments) {
        return templateRegistry.findById(name, "default")
                .map(template -> {
                    List<PromptVariableValue> variables = arguments.entrySet().stream()
                            .map(entry -> new PromptVariableValue(
                                    entry.getKey(),
                                    entry.getValue(),
                                    tech.kayys.wayang.prompt.core.PromptVariableDefinition.VariableSource.INPUT,
                                    false,
                                    System.currentTimeMillis()))
                            .collect(Collectors.toList());

                    PromptVersion activeVersion = template.resolveActiveVersion()
                            .orElseThrow(() -> new RuntimeException("No active version for template: " + name));

                    RenderingEngine engine = renderingEngineRegistry.forStrategy(activeVersion.getRenderingStrategy());
                    return engine.expand(template.getBody(), variables);
                })
                .onFailure().transform(e -> new RuntimeException("Failed to render prompt: " + name, e));
    }

    private MCPPrompt convertToMCPPrompt(PromptTemplate template) {
        Set<String> placeholders = template.getPlaceholders();
        List<MCPPrompt.PromptArgument> arguments = placeholders.stream()
                .map(p -> new MCPPrompt.PromptArgument(
                        p,
                        "Template variable",
                        true))
                .collect(Collectors.toList());

        // Convert Map<String, String> to Map<String, Object>
        Map<String, Object> mcpMetadata = template.getMetadata() != null ? template.getMetadata().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : Map.of();

        return MCPPrompt.builder()
                .name(template.getTemplateId())
                .description(template.getDescription())
                .arguments(arguments)
                .template(template.getBody())
                .metadata(mcpMetadata)
                .build();
    }

    private PromptTemplate convertToPromptTemplate(MCPPrompt mcpPrompt) {
        Instant now = Instant.now();

        // Convert Map<String, Object> to Map<String, String>
        Map<String, String> promptMetadata = mcpPrompt.getMetadata() != null
                ? mcpPrompt.getMetadata().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue() != null ? e.getValue().toString() : ""))
                : Map.of();

        return new PromptTemplate(
                mcpPrompt.getName(),
                mcpPrompt.getName(),
                mcpPrompt.getDescription(),
                "default",
                null,
                PromptTemplate.TemplateStatus.DRAFT,
                List.of(),
                List.of(),
                List.of(),
                "system",
                now,
                "system",
                now,
                promptMetadata);
    }
}
