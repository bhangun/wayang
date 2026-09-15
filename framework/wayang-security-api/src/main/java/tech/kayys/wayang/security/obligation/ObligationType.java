package tech.kayys.wayang.security.obligation;

import java.util.Objects;

/**
 * Extensible identifier for an obligation type.
 */
public record ObligationType(String value) {

    public ObligationType {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Obligation type must not be blank");
        }
    }

    public static ObligationType of(String value) {
        return new ObligationType(value);
    }
}
