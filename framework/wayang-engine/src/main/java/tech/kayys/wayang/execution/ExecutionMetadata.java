package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Metadata for an execution.
 */
public final class ExecutionMetadata {

    private final String name;
    private final String description;
    private final String author;
    private final String version;
    private final Instant createdAt;
    private final Map<String, Object> tags;
    private final Map<String, Object> custom;

    public ExecutionMetadata(String name, String description, String author, String version,
            Instant createdAt, Map<String, Object> tags, Map<String, Object> custom) {
        this.name = name;
        this.description = description;
        this.author = author;
        this.version = version;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.tags = tags != null ? Map.copyOf(tags) : Map.of();
        this.custom = custom != null ? Map.copyOf(custom) : Map.of();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public Map<String, Object> getCustom() {
        return custom;
    }

    public static ExecutionMetadata empty() {
        return new ExecutionMetadata(null, null, null, null, null, Map.of(), Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private String author;
        private String version;
        private Instant createdAt;
        private Map<String, Object> tags = Map.of();
        private Map<String, Object> custom = Map.of();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder tags(Map<String, Object> tags) {
            this.tags = tags;
            return this;
        }

        public Builder custom(Map<String, Object> custom) {
            this.custom = custom;
            return this;
        }

        public Builder putTag(String key, Object value) {
            if (this.tags == Map.of()) {
                this.tags = new java.util.HashMap<>();
            }
            this.tags.put(key, value);
            return this;
        }

        public Builder putCustom(String key, Object value) {
            if (this.custom == Map.of()) {
                this.custom = new java.util.HashMap<>();
            }
            this.custom.put(key, value);
            return this;
        }

        public ExecutionMetadata build() {
            return new ExecutionMetadata(name, description, author, version, createdAt, tags, custom);
        }
    }
}