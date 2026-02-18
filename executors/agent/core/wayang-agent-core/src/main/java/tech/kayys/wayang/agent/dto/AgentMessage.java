package tech.kayys.wayang.agent;

import java.time.Instant;
import java.util.Map;

import tech.kayys.wayang.agent.orchestrator.MessageType;

/**
 * Agent Message
 */
public record AgentMessage(
        String messageId,
        String fromAgentId,
        String toAgentId,
        MessageType type,
        String content,
        Map<String, Object> payload,
        String conversationId,
        Instant sentAt) {
}
