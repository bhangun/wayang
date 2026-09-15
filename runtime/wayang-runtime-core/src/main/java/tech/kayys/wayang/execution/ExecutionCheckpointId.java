package tech.kayys.wayang.execution;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable identifier for a lifecycle checkpoint.
 */
public record ExecutionCheckpointId(String value) {

    public ExecutionCheckpointId {
        Objects.requireNonNull(value, "Checkpoint id must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                "Checkpoint id must not be blank"
            );
        }
    }

    public static ExecutionCheckpointId create() {
        return new ExecutionCheckpointId(
            UUID.randomUUID().toString()
        );
    }

    @Override
    public String toString() {
        return value;
    }
}
