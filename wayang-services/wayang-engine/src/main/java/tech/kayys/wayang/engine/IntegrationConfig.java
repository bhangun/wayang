package tech.kayys.wayang.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration configuration for connector-based workflows.
 */
public class IntegrationConfig {
    private String sourceConnector;
    private String targetConnector;
    private String transformationRules;
    private ErrorStrategy errorStrategy = ErrorStrategy.RETRY;
    private String idempotencyKey;
    private Map<String, Object> connectorParams = new HashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IntegrationConfig config = new IntegrationConfig();

        public Builder sourceConnector(String connector) {
            config.sourceConnector = connector;
            return this;
        }

        public Builder targetConnector(String connector) {
            config.targetConnector = connector;
            return this;
        }

        public Builder transformationRules(String rules) {
            config.transformationRules = rules;
            return this;
        }

        public Builder errorStrategy(ErrorStrategy strategy) {
            config.errorStrategy = strategy;
            return this;
        }

        public Builder idempotencyKey(String key) {
            config.idempotencyKey = key;
            return this;
        }

        public Builder connectorParam(String key, Object value) {
            config.connectorParams.put(key, value);
            return this;
        }

        public IntegrationConfig build() {
            return config;
        }
    }

    // Getters and setters
    public String getSourceConnector() {
        return sourceConnector;
    }

    public void setSourceConnector(String sourceConnector) {
        this.sourceConnector = sourceConnector;
    }

    public String getTargetConnector() {
        return targetConnector;
    }

    public void setTargetConnector(String targetConnector) {
        this.targetConnector = targetConnector;
    }

    public String getTransformationRules() {
        return transformationRules;
    }

    public void setTransformationRules(String rules) {
        this.transformationRules = rules;
    }

    public ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }

    public void setErrorStrategy(ErrorStrategy strategy) {
        this.errorStrategy = strategy;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String key) {
        this.idempotencyKey = key;
    }

    public Map<String, Object> getConnectorParams() {
        return connectorParams;
    }

    public void setConnectorParams(Map<String, Object> params) {
        this.connectorParams = params;
    }
}
