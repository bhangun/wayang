package tech.kayys.wayang.node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tech.kayys.wayang.node.model.ValidationResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node Type Descriptor with complete schema information.
 * 
 * Includes:
 * - Basic metadata (id, name, description, category)
 * - UI metadata (icon, color)
 * - Input/output port schemas
 * - Configuration properties
 * - Error handling specifications
 * - Documentation
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeTypeDescriptor {

    private String id;
    private String name;
    private String description;
    private String category;
    private String icon;
    private String color;
    private List<PortSchema> inputs;
    private List<PortSchema> outputs;
    private List<PropertySchema> properties;
    private ErrorHandlingSpec errorHandling;
    private String documentation;
    private List<String> tags;
    private Map<String, Object> metadata;

    // Constructors
    public NodeTypeDescriptor() {
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NodeTypeDescriptor descriptor;

        public Builder() {
            this.descriptor = new NodeTypeDescriptor();
        }

        public Builder id(String id) {
            descriptor.id = id;
            return this;
        }

        public Builder name(String name) {
            descriptor.name = name;
            return this;
        }

        public Builder description(String description) {
            descriptor.description = description;
            return this;
        }

        public Builder category(String category) {
            descriptor.category = category;
            return this;
        }

        public Builder icon(String icon) {
            descriptor.icon = icon;
            return this;
        }

        public Builder color(String color) {
            descriptor.color = color;
            return this;
        }

        public Builder addInput(String name, String type, boolean required, String description) {
            descriptor.inputs.add(new PortSchema(name, type, required, description));
            return this;
        }

        public Builder addOutput(String portName, String name, String type, String description) {
            descriptor.outputs.add(new PortSchema(portName, name, type, description));
            return this;
        }

        public Builder addProperty(String name, String type, Object defaultValue,
                boolean required, String description) {
            descriptor.properties.add(
                    new PropertySchema(name, type, defaultValue, required, description));
            return this;
        }

        public Builder errorHandling(int maxRetries, String retryStrategy, boolean enabled) {
            descriptor.errorHandling = new ErrorHandlingSpec(maxRetries, retryStrategy, enabled);
            return this;
        }

        public Builder documentation(String documentation) {
            descriptor.documentation = documentation;
            return this;
        }

        public Builder addTag(String tag) {
            descriptor.tags.add(tag);
            return this;
        }

        public Builder metadata(String key, Object value) {
            descriptor.metadata.put(key, value);
            return this;
        }

        public NodeTypeDescriptor build() {
            return descriptor;
        }
    }

    /**
     * Validate node configuration against this descriptor.
     */
    public ValidationResult validateConfig(NodeConfigRequest config) {
        List<String> errors = new ArrayList<>();

        // Basic validation: check required properties
        for (PropertySchema property : properties) {
            if (property.isRequired() && !config.getConfig().containsKey(property.getName())) {
                errors.add("Missing required property: " + property.getName());
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            return ValidationResult.invalid(errors);
        }
    }

    // Inner classes for schemas
    public static class PortSchema {
        private String name;
        private String portName;
        private String type;
        private boolean required;
        private String description;

        public PortSchema() {
        }

        public PortSchema(String name, String type, boolean required, String description) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.description = description;
        }

        public PortSchema(String portName, String name, String type, String description) {
            this.portName = portName;
            this.name = name;
            this.type = type;
            this.description = description;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class PropertySchema {
        private String name;
        private String type;
        private Object defaultValue;
        private boolean required;
        private String description;
        private Object constraints;

        public PropertySchema() {
        }

        public PropertySchema(String name, String type, Object defaultValue,
                boolean required, String description) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.required = required;
            this.description = description;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Object getConstraints() {
            return constraints;
        }

        public void setConstraints(Object constraints) {
            this.constraints = constraints;
        }
    }

    public static class ErrorHandlingSpec {
        private int maxRetries;
        private String retryStrategy;
        private boolean enabled;

        public ErrorHandlingSpec() {
        }

        public ErrorHandlingSpec(int maxRetries, String retryStrategy, boolean enabled) {
            this.maxRetries = maxRetries;
            this.retryStrategy = retryStrategy;
            this.enabled = enabled;
        }

        // Getters and setters
        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public String getRetryStrategy() {
            return retryStrategy;
        }

        public void setRetryStrategy(String retryStrategy) {
            this.retryStrategy = retryStrategy;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<PortSchema> getInputs() {
        return inputs;
    }

    public void setInputs(List<PortSchema> inputs) {
        this.inputs = inputs;
    }

    public List<PortSchema> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<PortSchema> outputs) {
        this.outputs = outputs;
    }

    public List<PropertySchema> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertySchema> properties) {
        this.properties = properties;
    }

    public ErrorHandlingSpec getErrorHandling() {
        return errorHandling;
    }

    public void setErrorHandling(ErrorHandlingSpec errorHandling) {
        this.errorHandling = errorHandling;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
