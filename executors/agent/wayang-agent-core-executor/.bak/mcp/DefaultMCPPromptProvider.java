package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.mcp.MCPPromptProvider;
import tech.kayys.wayang.agent.mcp.model.MCPPrompt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of MCPPromptProvider.
 * Manages prompt templates with simple variable substitution.
 */
@ApplicationScoped
public class DefaultMCPPromptProvider implements MCPPromptProvider {

    private final Map<String, MCPPrompt> prompts = new ConcurrentHashMap<>();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    @Override
    public Uni<List<MCPPrompt>> listPrompts() {
        return Uni.createFrom().item(() -> List.copyOf(prompts.values()));
    }

    @Override
    public Uni<MCPPrompt> getPrompt(String name) {
        return Uni.createFrom().item(() -> {
            MCPPrompt prompt = prompts.get(name);
            if (prompt == null) {
                throw new IllegalArgumentException("Prompt not found: " + name);
            }
            return prompt;
        });
    }

    @Override
    public Uni<String> renderPrompt(String name, Map<String, Object> arguments) {
        return getPrompt(name)
                .map(prompt -> {
                    String template = prompt.getTemplate();

                    // Validate required arguments
                    for (MCPPrompt.PromptArgument arg : prompt.getArguments()) {
                        if (arg.isRequired() && !arguments.containsKey(arg.getName())) {
                            throw new IllegalArgumentException(
                                    "Missing required argument: " + arg.getName());
                        }
                    }

                    // Simple variable substitution
                    Matcher matcher = VARIABLE_PATTERN.matcher(template);
                    StringBuffer result = new StringBuffer();

                    while (matcher.find()) {
                        String varName = matcher.group(1);
                        Object value = arguments.get(varName);
                        String replacement = value != null ? value.toString() : "";
                        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                    }
                    matcher.appendTail(result);

                    return result.toString();
                });
    }

    @Override
    public Uni<Void> registerPrompt(MCPPrompt prompt) {
        return Uni.createFrom().item(() -> {
            prompts.put(prompt.getName(), prompt);
            return null;
        });
    }

    @Override
    public Uni<Void> unregisterPrompt(String name) {
        return Uni.createFrom().item(() -> {
            prompts.remove(name);
            return null;
        });
    }

    /**
     * Register a simple prompt template.
     */
    public void registerSimplePrompt(String name, String description, String template) {
        MCPPrompt prompt = MCPPrompt.builder()
                .name(name)
                .description(description)
                .template(template)
                .build();
        prompts.put(name, prompt);
    }
}
