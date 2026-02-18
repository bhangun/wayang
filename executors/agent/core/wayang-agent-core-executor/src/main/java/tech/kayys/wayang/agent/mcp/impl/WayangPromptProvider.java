package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.mcp.MCPPromptProvider;
import tech.kayys.wayang.agent.mcp.model.MCPPrompt;
import tech.kayys.wayang.prompt.core.*;
import tech.kayys.wayang.prompt.registry.PromptTemplateRegistry;

import java.util.List;
import java.util.Map;
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
        return templateRegistry.listAllTemplates("default") // Use default tenant
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
        return Uni.createFrom().item(() -> {
            // Check MCP prompts first
            if (mcpPrompts.containsKey(name)) {
                return mcpPrompts.get(name);
            }

            // Try to get from wayang-prompt registry
            return templateRegistry.getTemplate(name, "default")
                    .map(this::convertToMCPPrompt)
                    .await().indefinitely();
        });
    }

    @Override
    public Uni<String> renderPrompt(String name, Map<String, Object> arguments) {
        return Uni.createFrom().item(() -> {
            // Check if it's an MCP prompt
            if (mcpPrompts.containsKey(name)) {
                return renderMCPPrompt(mcpPrompts.get(name), arguments);
            }

            // Use wayang-prompt engine
            return renderWayangPrompt(name, arguments);
        });
    }

    @Override
    public Uni<Void> registerPrompt(MCPPrompt prompt) {
        return Uni.createFrom().item(() -> {
            mcpPrompts.put(prompt.getName(), prompt);

            // Optionally register in wayang-prompt registry
            PromptTemplate template = convertToPromptTemplate(prompt);
            templateRegistry.registerTemplate(template);

            return null;
        });
    }

    @Override
    public Uni<Void> unregisterPrompt(String name) {
        return Uni.createFrom().item(() -> {
            mcpPrompts.remove(name);
            return null;
        });
    }

    /**
     * Render MCP prompt with simple variable substitution.
     */
    private String renderMCPPrompt(MCPPrompt prompt, Map<String, Object> arguments) {
        // Validate required arguments
        for (MCPPrompt.PromptArgument arg : prompt.getArguments()) {
            if (arg.isRequired() && !arguments.containsKey(arg.getName())) {
                throw new IllegalArgumentException(
                        "Missing required argument: " + arg.getName());
            }
        }

        // Simple variable substitution
        String result = prompt.getTemplate();
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * Render using wayang-prompt engine with full template engine support.
     */
    private String renderWayangPrompt(String name, Map<String, Object> arguments) {
        try {
            // Get the template
            PromptTemplate template = templateRegistry.getTemplate(name, "default")
                    .await().indefinitely();

            // Convert arguments to PromptVariableValue list
            List<PromptVariableValue> variables = arguments.entrySet().stream()
                    .map(entry -> new PromptVariableValue(
                            entry.getKey(),
                            entry.getValue() != null ? entry.getValue().toString() : ""))
                    .collect(Collectors.toList());

            // Determine rendering strategy
            RenderingEngine engine = getRenderingEngine(template);

            // Render the template
            return engine.expand(template.getBody(), variables);

        } catch (Exception e) {
            throw new RuntimeException("Failed to render prompt: " + name, e);
        }
    }

    /**
     * Get appropriate rendering engine for template.
     */
    private RenderingEngine getRenderingEngine(PromptTemplate template) {
        // Check metadata for rendering strategy hint
        String strategy = template.getMetadata().getOrDefault("renderingStrategy", "SIMPLE");

        return switch (strategy.toUpperCase()) {
            case "JINJA2" -> renderingEngineRegistry.getEngine(PromptVersion.RenderingStrategy.JINJA2);
            case "FREEMARKER" -> renderingEngineRegistry.getEngine(PromptVersion.RenderingStrategy.FREEMARKER);
            default -> renderingEngineRegistry.getEngine(PromptVersion.RenderingStrategy.SIMPLE);
        };
    }

    /**
     * Convert PromptTemplate to MCPPrompt.
     */
    private MCPPrompt convertToMCPPrompt(PromptTemplate template) {
        List<MCPPrompt.PromptArgument> arguments = template.getVariables().stream()
                .map(var -> new MCPPrompt.PromptArgument(
                        var.name(),
                        var.description(),
                        var.required()))
                .collect(Collectors.toList());

        return MCPPrompt.builder()
                .name(template.getId())
                .description(template.getMetadata().getOrDefault("description", ""))
                .arguments(arguments)
                .template(template.getBody())
                .metadata(template.getMetadata())
                .build();
    }

    /**
     * Convert MCPPrompt to PromptTemplate.
     */
    private PromptTemplate convertToPromptTemplate(MCPPrompt mcpPrompt) {
        List<PromptTemplate.VariableDescriptor> variables = mcpPrompt.getArguments().stream()
                .map(arg -> new PromptTemplate.VariableDescriptor(
                        arg.getName(),
                        arg.getDescription(),
                        arg.isRequired(),
                        null // default value
                ))
                .collect(Collectors.toList());

        return PromptTemplate.builder()
                .id(mcpPrompt.getName())
                .version("1.0.0")
                .tenantId("default")
                .status(PromptTemplate.TemplateStatus.PUBLISHED)
                .role(PromptRole.SYSTEM) // Default role
                .body(mcpPrompt.getTemplate())
                .variables(variables)
                .metadata(mcpPrompt.getMetadata())
                .build();
    }
}
