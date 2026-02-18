package tech.kayys.wayang.agent.a2a.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.a2a.AgentCommunicationProtocol;
import tech.kayys.wayang.agent.a2a.model.AgentMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of Agent Communication Protocol.
 * Provides in-memory message passing for agent-to-agent communication.
 */
@ApplicationScoped
public class DefaultAgentCommunicationProtocol implements AgentCommunicationProtocol {

    private final Map<String, List<MessageHandler>> subscribers = new ConcurrentHashMap<>();
    private final Map<String, AgentCapabilities> agentCapabilities = new ConcurrentHashMap<>();

    @Override
    public Uni<Void> sendMessage(AgentMessage message) {
        return Uni.createFrom().item(() -> {
            String targetAgent = message.getToAgentId();
            if (targetAgent != null && subscribers.containsKey(targetAgent)) {
                List<MessageHandler> handlers = subscribers.get(targetAgent);
                for (MessageHandler handler : handlers) {
                    handler.handle(message).subscribe().with(
                            response -> {
                            }, // Message sent successfully
                            error -> {
                            } // Handle error silently for now
                    );
                }
            }
            return null;
        });
    }

    @Override
    public Uni<AgentMessage> sendAndReceive(AgentMessage message) {
        return sendMessage(message)
                .chain(() -> {
                    // Wait for response with correlation ID
                    // This is a simplified implementation
                    // In production, use proper async messaging with correlation
                    return Uni.createFrom().nullItem();
                });
    }

    @Override
    public Uni<Void> broadcast(AgentMessage message, List<String> targetAgentIds) {
        return Uni.combine().all().unis(
                targetAgentIds.stream()
                        .map(agentId -> {
                            AgentMessage broadcastMsg = AgentMessage.builder()
                                    .fromAgentId(message.getFromAgentId())
                                    .toAgentId(agentId)
                                    .type(AgentMessage.MessageType.BROADCAST)
                                    .subject(message.getSubject())
                                    .payload(message.getPayload())
                                    .correlationId(message.getCorrelationId())
                                    .build();
                            return sendMessage(broadcastMsg);
                        })
                        .toList())
                .discardItems();
    }

    @Override
    public Subscription subscribe(String agentId, MessageHandler handler) {
        subscribers.computeIfAbsent(agentId, k -> new CopyOnWriteArrayList<>())
                .add(handler);

        return () -> {
            List<MessageHandler> handlers = subscribers.get(agentId);
            if (handlers != null) {
                handlers.remove(handler);
            }
        };
    }

    @Override
    public Uni<List<String>> discoverAgents() {
        return Uni.createFrom().item(() -> List.copyOf(agentCapabilities.keySet()));
    }

    @Override
    public Uni<AgentCapabilities> getCapabilities(String agentId) {
        return Uni.createFrom().item(() -> {
            AgentCapabilities capabilities = agentCapabilities.get(agentId);
            if (capabilities == null) {
                throw new IllegalArgumentException("Agent not found: " + agentId);
            }
            return capabilities;
        });
    }

    /**
     * Register agent capabilities.
     * This should be called when an agent starts up.
     */
    public void registerAgent(AgentCapabilities capabilities) {
        agentCapabilities.put(capabilities.getAgentId(), capabilities);
    }

    /**
     * Unregister an agent.
     */
    public void unregisterAgent(String agentId) {
        agentCapabilities.remove(agentId);
        subscribers.remove(agentId);
    }
}
