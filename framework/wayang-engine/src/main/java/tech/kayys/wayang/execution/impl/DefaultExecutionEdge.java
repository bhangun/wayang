package tech.kayys.wayang.execution.impl;

import java.util.Optional;
import java.util.UUID;

import tech.kayys.wayang.execution.EdgeCondition;
import tech.kayys.wayang.execution.ExecutionEdge;

/**
 * Default implementation of ExecutionEdge.
 */
public final class DefaultExecutionEdge implements ExecutionEdge {

    private final UUID from;
    private final UUID to;
    private final EdgeCondition condition;
    private final String label;

    public DefaultExecutionEdge(UUID from, UUID to, EdgeCondition condition) {
        this(from, to, condition, null);
    }

    public DefaultExecutionEdge(UUID from, UUID to, EdgeCondition condition, String label) {
        this.from = from;
        this.to = to;
        this.condition = condition != null ? condition : EdgeCondition.always();
        this.label = label;
    }

    @Override
    public UUID from() {
        return from;
    }

    @Override
    public UUID to() {
        return to;
    }

    @Override
    public EdgeCondition condition() {
        return condition;
    }

    @Override
    public Optional<String> label() {
        return Optional.ofNullable(label);
    }

    @Override
    public double weight() {
        return 1.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID from;
        private UUID to;
        private EdgeCondition condition;
        private String label;

        public Builder from(UUID from) {
            this.from = from;
            return this;
        }

        public Builder to(UUID to) {
            this.to = to;
            return this;
        }

        public Builder condition(EdgeCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public DefaultExecutionEdge build() {
            return new DefaultExecutionEdge(from, to, condition, label);
        }
    }
}