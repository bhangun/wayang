package tech.kayys.wayang.agent.mcp.model;

import java.util.List;
import java.util.Map;

/**
 * Represents an MCP prompt template.
 * Prompts are reusable templates with arguments that can be rendered with
 * context.
 */
public class MCPPrompt {
    private final String name;
    private final String description;
    private final List<PromptArgument> arguments;
    private final String template;
    private final Map<String, Object> metadata;

    private MCPPrompt(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.arguments = builder.arguments;
        this.template = builder.template;
        this.metadata = builder.metadata;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<PromptArgument> getArguments() {
        return arguments;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private List<PromptArgument> arguments = List.of();
        private String template;
        private Map<String, Object> metadata = Map.of();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder arguments(List<PromptArgument> arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder template(String template) {
            this.template = template;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public MCPPrompt build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (template == null || template.isEmpty()) {
                throw new IllegalArgumentException("Template is required");
            }
            return new MCPPrompt(this);
        }
    }

    /**
     * Represents a prompt argument.
     */
    public static class PromptArgument {
        private final String name;
        private final String description;
        private final boolean required;

        public PromptArgument(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
