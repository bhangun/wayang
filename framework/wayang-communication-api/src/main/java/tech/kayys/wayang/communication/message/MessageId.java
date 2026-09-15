package tech.kayys.wayang.communication.message;

import java.util.Objects;
import java.util.UUID;

public record MessageId(
        UUID value
) {

    public MessageId {
        Objects.requireNonNull(value, "value");
    }

    public static MessageId random() {
        return new MessageId(UUID.randomUUID());
    }

    public static MessageId of(UUID value) {
        return new MessageId(value);
    }

    public static MessageId of(String value) {
        return new MessageId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
