package tech.kayys.wayang.agent.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.agent.mcp.model.MCPPrompt;

import java.util.List;
import java.util.Map;

/**
 * MCP Prompt Provider interface.
 * Manages prompt templates for agents.
 */
public interface MCPPromptProvider {

    /**
     * List all available prompts.
     * 
     * @return List of available prompts
     */
    Uni<List<MCPPrompt>> listPrompts();

    /**
     * Get a prompt by name.
     * 
     * @param name The prompt name
     * @return The prompt
     */
    Uni<MCPPrompt> getPrompt(String name);

    /**
     * Render a prompt with arguments.
     * 
     * @param name      The prompt name
     * @param arguments Arguments to render with
     * @return Rendered prompt text
     */
    Uni<String> renderPrompt(String name, Map<String, Object> arguments);

    /**
     * Register a new prompt.
     * 
     * @param prompt The prompt to register
     * @return Uni that completes when registered
     */
    Uni<Void> registerPrompt(MCPPrompt prompt);

    /**
     * Unregister a prompt.
     * 
     * @param name The prompt name
     * @return Uni that completes when unregistered
     */
    Uni<Void> unregisterPrompt(String name);
}
