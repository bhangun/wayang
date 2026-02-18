package tech.kayys.wayang.agent.mcp.model;

import java.util.Map;

/**
 * Represents an MCP resource.
 * Resources are content that can be accessed by agents (files, data, APIs,
 * etc.)
 */
public class MCPResource {
    private final String uri;
    private final String name;
    private final String description;
    private final String mimeType;
    private final Map<String, Object> annotations;

    private MCPResource(Builder builder) {
        this.uri = builder.uri;
        this.name = builder.name;
        this.description = builder.description;
        this.mimeType = builder.mimeType;
        this.annotations = builder.annotations;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uri;
        private String name;
        private String description;
        private String mimeType = "text/plain";
        private Map<String, Object> annotations = Map.of();

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder annotations(Map<String, Object> annotations) {
            this.annotations = annotations;
            return this;
        }

        public MCPResource build() {
            if (uri == null || uri.isEmpty()) {
                throw new IllegalArgumentException("URI is required");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            return new MCPResource(this);
        }
    }
}
