package tech.kayys.wayang.security.delegation;

import java.util.Objects;
import java.util.UUID;

public record DelegationId(String value) {

    public DelegationId {
        Objects.requireNonNull(value, "value");
    }

    public static DelegationId generate() {
        return new DelegationId("del-" + UUID.randomUUID());
    }

    public static DelegationId of(String value) {
        return new DelegationId(value);
    }
}
