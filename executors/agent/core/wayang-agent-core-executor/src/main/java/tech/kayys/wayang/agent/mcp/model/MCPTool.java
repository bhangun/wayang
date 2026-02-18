package tech.kayys.wayang.agent.mcp.model;

import java.util.Map;

/**
 * Represents an MCP-compliant tool definition.
 * Tools are functions that agents can call to perform actions.
 */
public class MCPTool {
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema;
    private final Map<String, Object> annotations;

    private MCPTool(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.inputSchema = builder.inputSchema;
        this.annotations = builder.annotations;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private Map<String, Object> inputSchema = Map.of();
        private Map<String, Object> annotations = Map.of();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(Map<String, Object> inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }

        public Builder annotations(Map<String, Object> annotations) {
            this.annotations = annotations;
            return this;
        }

        public MCPTool build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (description == null || description.isEmpty()) {
                throw new IllegalArgumentException("Description is required");
            }
            return new MCPTool(this);
        }
    }
}
