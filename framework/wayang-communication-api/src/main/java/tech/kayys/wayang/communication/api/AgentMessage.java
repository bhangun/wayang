package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.message.MessageId;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.communication.message.MessageType;

import java.util.Map;
import java.util.Objects;

public record AgentMessage(
        MessageId id,
        AgentRef sender,
        AgentRef recipient,
        MessageType type,
        MessagePayload payload,
        Map<String, Object> metadata
) {

    public AgentMessage {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }
}
