package tech.kayys.wayang.agent.a2a;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.agent.a2a.model.AgentMessage;

import java.util.List;

/**
 * Agent Communication Protocol (ACP) interface.
 * Defines standard methods for agent-to-agent communication.
 */
public interface AgentCommunicationProtocol {

    /**
     * Send a message to another agent.
     * 
     * @param message The message to send
     * @return Uni that completes when message is sent
     */
    Uni<Void> sendMessage(AgentMessage message);

    /**
     * Send a message and wait for a response.
     * 
     * @param message The message to send
     * @return Uni with the response message
     */
    Uni<AgentMessage> sendAndReceive(AgentMessage message);

    /**
     * Broadcast a message to multiple agents.
     * 
     * @param message        The message to broadcast
     * @param targetAgentIds List of target agent IDs
     * @return Uni that completes when broadcast is sent
     */
    Uni<Void> broadcast(AgentMessage message, List<String> targetAgentIds);

    /**
     * Subscribe to messages for this agent.
     * 
     * @param agentId The agent ID to subscribe for
     * @param handler Message handler
     * @return Subscription handle
     */
    Subscription subscribe(String agentId, MessageHandler handler);

    /**
     * Discover available agents.
     * 
     * @return List of available agent IDs
     */
    Uni<List<String>> discoverAgents();

    /**
     * Get agent capabilities.
     * 
     * @param agentId The agent ID
     * @return Agent capabilities
     */
    Uni<AgentCapabilities> getCapabilities(String agentId);

    /**
     * Handler for incoming messages.
     */
    @FunctionalInterface
    interface MessageHandler {
        Uni<AgentMessage> handle(AgentMessage message);
    }

    /**
     * Subscription handle for message subscriptions.
     */
    interface Subscription {
        void cancel();
    }

    /**
     * Agent capabilities descriptor.
     */
    class AgentCapabilities {
        private final String agentId;
        private final String agentType;
        private final List<String> supportedProtocols;
        private final List<String> availableTools;
        private final List<String> supportedFormats;

        public AgentCapabilities(String agentId, String agentType,
                List<String> supportedProtocols,
                List<String> availableTools,
                List<String> supportedFormats) {
            this.agentId = agentId;
            this.agentType = agentType;
            this.supportedProtocols = supportedProtocols;
            this.availableTools = availableTools;
            this.supportedFormats = supportedFormats;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getAgentType() {
            return agentType;
        }

        public List<String> getSupportedProtocols() {
            return supportedProtocols;
        }

        public List<String> getAvailableTools() {
            return availableTools;
        }

        public List<String> getSupportedFormats() {
            return supportedFormats;
        }
    }
}
