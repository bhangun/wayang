package tech.kayys.wayang.communication.capability;

import java.util.Objects;

public record CapabilityId(
        String value
) {

    public CapabilityId {
        Objects.requireNonNull(value, "value");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Capability id must not be blank"
            );
        }
    }

    public static CapabilityId of(String value) {
        return new CapabilityId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
