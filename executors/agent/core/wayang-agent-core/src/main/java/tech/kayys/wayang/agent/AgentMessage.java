package tech.kayys.wayang.agent;

import java.time.Instant;
import java.util.Map;

/**
 * Agent Message - Communication between agents.
 */
public record AgentMessage(
        String messageId,
        String fromAgentId,
        String toAgentId,
        String type, // e.g. "REQUEST", "RESPONSE", "SIGNAL"
        String content,
        Map<String, Object> payload,
        String conversationId,
        Instant sentAt) {
}
