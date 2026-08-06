package tech.kayys.wayang.agent;

import tech.kayys.wayang.provider.ChatMessage;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.tool.Tool;

import java.util.Collection;
import java.util.List;

/**
 * Core interface for an Agent in the Wayang Platform.
 */
public interface Agent {

    /**
     * Sends a user input to the agent and drives the conversation to completion.
     */
    void send(String userInput, WayangAgentListener listener);

    /**
     * Returns the current conversation history.
     */
    List<ChatMessage> history();

    /**
     * Clears the current conversation history.
     */
    void clearHistory();

    /**
     * Replaces the current conversation history with the provided messages.
     */
    void replaceHistory(List<ChatMessage> newHistory);

    /**
     * Sets the provider for the agent.
     */
    void setProvider(Provider provider);

    /**
     * Sets the model ID for the provider.
     */
    void setModelId(String modelId);

    /**
     * Gets the current model ID.
     */
    String getModelId();

    /**
     * Sets the system prompt.
     */
    void setSystemPrompt(String sp);

    /**
     * Gets the tools available to the agent.
     */
    Collection<Tool> tools();

    /**
     * Sets the tools available to the agent.
     */
    void setTools(List<Tool> tools);

    /**
     * Whether mutating tools are automatically approved.
     */
    boolean autoApproveTools();

    /**
     * Sets whether mutating tools are automatically approved.
     */
    void setAutoApproveTools(boolean v);

    /**
     * The workspace path for the agent.
     */
    java.nio.file.Path workspace();
}
