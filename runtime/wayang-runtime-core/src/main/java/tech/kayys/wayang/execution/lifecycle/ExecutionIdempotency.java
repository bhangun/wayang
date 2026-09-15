package tech.kayys.wayang.execution.lifecycle;

public record ExecutionIdempotency(
    String key,
    ExecutionSemantics semantics
) {

    public ExecutionIdempotency {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be blank");
        }

        if (semantics == null) {
            throw new IllegalArgumentException("Execution semantics must not be null");
        }
    }
}
