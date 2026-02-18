package tech.kayys.wayang.agent.a2a.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message in Agent-to-Agent (A2A) communication.
 * Supports asynchronous, event-driven agent collaboration.
 */
public class AgentMessage {
    private final String messageId;
    private final String fromAgentId;
    private final String toAgentId;
    private final MessageType type;
    private final String subject;
    private final Map<String, Object> payload;
    private final Instant timestamp;
    private final String correlationId;
    private final String replyTo;

    private AgentMessage(Builder builder) {
        this.messageId = builder.messageId != null ? builder.messageId : UUID.randomUUID().toString();
        this.fromAgentId = builder.fromAgentId;
        this.toAgentId = builder.toAgentId;
        this.type = builder.type;
        this.subject = builder.subject;
        this.payload = builder.payload;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.correlationId = builder.correlationId;
        this.replyTo = builder.replyTo;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFromAgentId() {
        return fromAgentId;
    }

    public String getToAgentId() {
        return toAgentId;
    }

    public MessageType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String messageId;
        private String fromAgentId;
        private String toAgentId;
        private MessageType type = MessageType.REQUEST;
        private String subject;
        private Map<String, Object> payload = Map.of();
        private Instant timestamp;
        private String correlationId;
        private String replyTo;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder fromAgentId(String fromAgentId) {
            this.fromAgentId = fromAgentId;
            return this;
        }

        public Builder toAgentId(String toAgentId) {
            this.toAgentId = toAgentId;
            return this;
        }

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public AgentMessage build() {
            if (fromAgentId == null || fromAgentId.isEmpty()) {
                throw new IllegalArgumentException("fromAgentId is required");
            }
            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("subject is required");
            }
            return new AgentMessage(this);
        }
    }

    /**
     * Types of agent messages.
     */
    public enum MessageType {
        REQUEST, // Request for action/information
        RESPONSE, // Response to a request
        EVENT, // Event notification
        COMMAND, // Command to execute
        QUERY, // Query for information
        BROADCAST // Broadcast to multiple agents
    }
}
