package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.a2a.AgentCommunicationProtocol;
import tech.kayys.wayang.agent.a2a.model.AgentMessage;
import tech.kayys.wayang.agent.mcp.MCPPromptProvider;
import tech.kayys.wayang.agent.mcp.MCPResourceProvider;
import tech.kayys.wayang.agent.mcp.MCPToolProvider;
import tech.kayys.wayang.agent.model.AgentMemoryManager;
import tech.kayys.wayang.agent.model.ToolExecutor;
import tech.kayys.wayang.executor.gamelan.AbstractAgentExecutor;
import tech.kayys.wayang.executor.gamelan.ExecutionContext;

import java.util.Map;

/**
 * MCP-aware agent executor with full support for:
 * - MCP Resources, Prompts, and Tools
 * - Agent-to-Agent (A2A) communication
 * - Memory management
 * - Guardrails integration
 * 
 * This executor is future-proof and compliant with MCP, A2A, and ACP protocols.
 */
public abstract class MCPAgentExecutor extends AbstractAgentExecutor {

    @Inject
    protected MCPResourceProvider resourceProvider;

    @Inject
    protected MCPPromptProvider promptProvider;

    @Inject
    protected MCPToolProvider toolProvider;

    @Inject
    protected AgentCommunicationProtocol communicationProtocol;

    public MCPAgentExecutor(AgentMemoryManager memoryManager, ToolExecutor toolExecutor) {
        super(memoryManager, toolExecutor);
    }

    /**
     * Access MCP resources during execution.
     */
    protected Uni<String> readResource(String uri) {
        return resourceProvider.readResource(uri);
    }

    /**
     * Render MCP prompt with arguments.
     */
    protected Uni<String> renderPrompt(String promptName, Map<String, Object> arguments) {
        return promptProvider.renderPrompt(promptName, arguments);
    }

    /**
     * Execute MCP tool.
     */
    protected Uni<Object> executeMCPTool(String toolName, Map<String, Object> arguments) {
        return toolProvider.executeTool(toolName, arguments)
                .map(result -> result.getOutput());
    }

    /**
     * Send message to another agent.
     */
    protected Uni<Void> sendToAgent(String targetAgentId, String subject, Map<String, Object> payload) {
        AgentMessage message = AgentMessage.builder()
                .fromAgentId(getAgentId())
                .toAgentId(targetAgentId)
                .type(AgentMessage.MessageType.REQUEST)
                .subject(subject)
                .payload(payload)
                .build();

        return communicationProtocol.sendMessage(message);
    }

    /**
     * Broadcast message to multiple agents.
     */
    protected Uni<Void> broadcastToAgents(java.util.List<String> targetAgentIds,
            String subject,
            Map<String, Object> payload) {
        AgentMessage message = AgentMessage.builder()
                .fromAgentId(getAgentId())
                .type(AgentMessage.MessageType.BROADCAST)
                .subject(subject)
                .payload(payload)
                .build();

        return communicationProtocol.broadcast(message, targetAgentIds);
    }

    /**
     * Discover available agents.
     */
    protected Uni<java.util.List<String>> discoverAgents() {
        return communicationProtocol.discoverAgents();
    }

    /**
     * Get agent ID for this executor.
     * Subclasses should override to provide unique agent ID.
     */
    protected abstract String getAgentId();

    /**
     * Enhanced execution with MCP and A2A support.
     */
    @Override
    protected Uni<Map<String, Object>> executeInternal(ExecutionContext context) {
        // Register this agent for A2A communication
        registerForCommunication();

        // Execute with MCP capabilities
        return executeWithMCP(context)
                .eventually(() -> unregisterFromCommunication());
    }

    /**
     * Execute with MCP capabilities.
     * Subclasses should implement this instead of executeInternal.
     */
    protected abstract Uni<Map<String, Object>> executeWithMCP(ExecutionContext context);

    /**
     * Register this agent for A2A communication.
     */
    private void registerForCommunication() {
        if (communicationProtocol instanceof tech.kayys.wayang.agent.a2a.impl.DefaultAgentCommunicationProtocol) {
            var protocol = (tech.kayys.wayang.agent.a2a.impl.DefaultAgentCommunicationProtocol) communicationProtocol;

            AgentCommunicationProtocol.AgentCapabilities capabilities = new AgentCommunicationProtocol.AgentCapabilities(
                    getAgentId(),
                    getClass().getSimpleName(),
                    java.util.List.of("MCP", "A2A", "ACP"),
                    java.util.List.of(), // Tools will be populated from registry
                    java.util.List.of("json", "text"));

            protocol.registerAgent(capabilities);
        }
    }

    /**
     * Unregister from A2A communication.
     */
    private Uni<Void> unregisterFromCommunication() {
        if (communicationProtocol instanceof tech.kayys.wayang.agent.a2a.impl.DefaultAgentCommunicationProtocol) {
            var protocol = (tech.kayys.wayang.agent.a2a.impl.DefaultAgentCommunicationProtocol) communicationProtocol;
            protocol.unregisterAgent(getAgentId());
        }
        return Uni.createFrom().voidItem();
    }
}
