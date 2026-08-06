package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Optional;

/**
 * Configuration for a node.
 */
public final class NodeConfig {

    private final String description;
    private final Map<String, Object> parameters;
    private final Map<String, Object> options;

    public NodeConfig(String description, Map<String, Object> parameters, Map<String, Object> options) {
        this.description = description;
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.options = options != null ? Map.copyOf(options) : Map.of();
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public <T> Optional<T> getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public <T> Optional<T> getOption(String key, Class<T> type) {
        Object value = options.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public static NodeConfig empty() {
        return new NodeConfig(null, Map.of(), Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String description;
        private Map<String, Object> parameters = Map.of();
        private Map<String, Object> options = Map.of();

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder options(Map<String, Object> options) {
            this.options = options;
            return this;
        }

        public Builder put(String key, Object value) {
            if (this.parameters == Map.of()) {
                this.parameters = new java.util.HashMap<>();
            }
            this.parameters.put(key, value);
            return this;
        }

        public NodeConfig build() {
            return new NodeConfig(description, parameters, options);
        }
    }
}