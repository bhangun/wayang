package tech.kayys.wayang.communication.task;

import java.util.Objects;
import java.util.UUID;

public record TaskId(
        UUID value
) {

    public TaskId {
        Objects.requireNonNull(value, "value");
    }

    public static TaskId random() {
        return new TaskId(UUID.randomUUID());
    }

    public static TaskId of(UUID value) {
        return new TaskId(value);
    }

    public static TaskId of(String value) {
        return new TaskId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
