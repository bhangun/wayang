package tech.kayys.wayang.plugin;

import java.util.Map;

/**
 * Descriptor for a node port (input or output)
 */
public class PortDescriptor {
    private String name;
    private String type;
    private boolean required;
    private String description;
    private Map<String, Object> schema;
    
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

public Map<String, Object> getSchema() {
    return schema;
}

public void setSchema(Map<String, Object> schema) {
    this.schema = schema;
}

public static class Builder {
    private String name;
    private String type;
    private boolean required;
    private String description;
    private Map<String, Object> schema;

    public Builder name(String name) {
        this.name = name;
        return this;
    }

    public Builder type(String type) {
        this.type = type;
        return this;
    }

    public Builder required(boolean required) {
        this.required = required;
        return this;
    }

    public Builder description(String description) {
        this.description = description;
        return this;
    }

    public Builder schema(Map<String, Object> schema) {
        this.schema = schema;
        return this;
    }

    public PortDescriptor build() {
        PortDescriptor portDescriptor = new PortDescriptor();
        portDescriptor.setName(name);
        portDescriptor.setType(type);
        portDescriptor.setRequired(required);
        portDescriptor.setDescription(description);
        portDescriptor.setSchema(schema);
        return portDescriptor;
    }
}
}
