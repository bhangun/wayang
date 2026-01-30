package tech.kayys.wayang.engine;

import java.time.Duration;

/**
 * Configuration for Gamelan Engine connection
 */
public record GamelanEngineConfig(
    String endpoint,
    String tenantId,
    String apiKey,
    Duration timeout
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String endpoint = "http://localhost:8080";
        private String tenantId;
        private String apiKey;
        private Duration timeout = Duration.ofSeconds(30);

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public GamelanEngineConfig build() {
            return new GamelanEngineConfig(endpoint, tenantId, apiKey, timeout);
        }
    }
}
