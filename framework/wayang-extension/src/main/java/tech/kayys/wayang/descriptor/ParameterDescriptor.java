package tech.kayys.wayang.descriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.*;

/**
 * Describes a parameter or input/output.
 */
public record ParameterDescriptor(
    String name,
    String displayName,
    String description,
    ParameterType type,
    boolean required,
    boolean multiple,
    List<Object> allowedValues,
    Object defaultValue,
    Map<String, Object> constraints
) {
    
    public static ParameterDescriptorBuilder builder() {
        return new ParameterDescriptorBuilder();
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public boolean isMultiple() {
        return multiple;
    }
    
    public boolean hasAllowedValues() {
        return allowedValues != null && !allowedValues.isEmpty();
    }
    
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }
    
    public static class ParameterDescriptorBuilder {
        private String name;
        private String displayName;
        private String description;
        private ParameterType type;
        private boolean required;
        private boolean multiple;
        private List<Object> allowedValues;
        private Object defaultValue;
        private final Map<String, Object> constraints = new HashMap<>();
        
        public ParameterDescriptorBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public ParameterDescriptorBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public ParameterDescriptorBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public ParameterDescriptorBuilder type(ParameterType type) {
            this.type = type;
            return this;
        }
        
        public ParameterDescriptorBuilder required(boolean required) {
            this.required = required;
            return this;
        }
        
        public ParameterDescriptorBuilder multiple(boolean multiple) {
            this.multiple = multiple;
            return this;
        }
        
        public ParameterDescriptorBuilder allowedValues(Object... values) {
            this.allowedValues = List.of(values);
            return this;
        }
        
        public ParameterDescriptorBuilder allowedValues(List<Object> values) {
            this.allowedValues = values;
            return this;
        }
        
        public ParameterDescriptorBuilder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public ParameterDescriptorBuilder constraint(String key, Object value) {
            this.constraints.put(key, value);
            return this;
        }
        
        public ParameterDescriptor build() {
            return new ParameterDescriptor(
                name,
                displayName,
                description,
                type,
                required,
                multiple,
                allowedValues,
                defaultValue,
                constraints
            );
        }
    }
}
