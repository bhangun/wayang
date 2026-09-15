package tech.kayys.wayang.communication.protocol;

import java.util.Objects;

public record ProtocolId(
        String value
) {

    public ProtocolId {
        Objects.requireNonNull(value, "value");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Protocol id must not be blank"
            );
        }
    }

    public static ProtocolId of(String value) {
        return new ProtocolId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
