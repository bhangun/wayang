// src/main/java/com/example/generator/plugin/PluginManager.java
package com.example.generator.plugin;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginManager {
    
    @Inject
    Instance<GeneratorPlugin> plugins;
    
    @Inject
    Instance<TemplateProcessor> templateProcessors;
    
    @Inject
    Instance<SpecificationProcessor> specProcessors;
    
    @Inject
    Instance<ValidationPlugin> validators;
    
    private final Map<String, GeneratorPlugin> pluginRegistry = new ConcurrentHashMap<>();
    private final Map<String, TemplateProcessor> templateRegistry = new ConcurrentHashMap<>();
    private final Map<String, SpecificationProcessor> specRegistry = new ConcurrentHashMap<>();
    private final Map<String, ValidationPlugin> validatorRegistry = new ConcurrentHashMap<>();
    
    public void initializePlugins() {
        Log.info("Initializing plugin system...");
        
        // Register generator plugins
        plugins.forEach(plugin -> {
            try {
                plugin.initialize();
                pluginRegistry.put(plugin.getId(), plugin);
                Log.info("Registered generator plugin: {} v{}", plugin.getName(), plugin.getVersion());
            } catch (Exception e) {
                Log.error("Failed to initialize plugin: " + plugin.getClass().getSimpleName(), e);
            }
        });
        
        // Register template processors
        templateProcessors.forEach(processor -> {
            try {
                processor.initialize();
                templateRegistry.put(processor.getTemplateType(), processor);
                Log.info("Registered template processor: {}", processor.getTemplateType());
            } catch (Exception e) {
                Log.error("Failed to initialize template processor: " + processor.getClass().getSimpleName(), e);
            }
        });
        
        // Register specification processors
        specProcessors.forEach(processor -> {
            try {
                processor.initialize();
                specRegistry.put(processor.getSpecificationType(), processor);
                Log.info("Registered spec processor: {}", processor.getSpecificationType());
            } catch (Exception e) {
                Log.error("Failed to initialize spec processor: " + processor.getClass().getSimpleName(), e);
            }
        });
        
        // Register validators
        validators.forEach(validator -> {
            try {
                validator.initialize();
                validatorRegistry.put(validator.getValidationType(), validator);
                Log.info("Registered validator: {}", validator.getValidationType());
            } catch (Exception e) {
                Log.error("Failed to initialize validator: " + validator.getClass().getSimpleName(), e);
            }
        });
        
        Log.info("Plugin system initialized with {} generator plugins, {} template processors, {} spec processors, {} validators",
                pluginRegistry.size(), templateRegistry.size(), specRegistry.size(), validatorRegistry.size());
    }
    
    public List<GeneratorPlugin> getAvailablePlugins() {
        return new ArrayList<>(pluginRegistry.values());
    }
    
    public Optional<GeneratorPlugin> getPlugin(String pluginId) {
        return Optional.ofNullable(pluginRegistry.get(pluginId));
    }
    
    public List<TemplateProcessor> getTemplateProcessors(String templateType) {
        return templateRegistry.values().stream()
            .filter(processor -> processor.supports(templateType))
            .collect(Collectors.toList());
    }
    
    public Optional<SpecificationProcessor> getSpecificationProcessor(String specType) {
        return Optional.ofNullable(specRegistry.get(specType));
    }
    
    public List<ValidationPlugin> getValidators(String validationType) {
        return validatorRegistry.values().stream()
            .filter(validator -> validator.supports(validationType))
            .collect(Collectors.toList());
    }
    
    public PluginExecutionContext createExecutionContext() {
        return new PluginExecutionContext();
    }
}

// src/main/java/com/example/generator/plugin/GeneratorPlugin.java
package com.example.generator.plugin;

import java.util.Map;

public interface GeneratorPlugin {
    
    String getId();
    String getName();
    String getVersion();
    String getDescription();
    
    void initialize() throws PluginException;
    void shutdown() throws PluginException;
    
    boolean supports(String operation);
    
    PluginResult execute(PluginExecutionContext context) throws PluginException;
    
    Map<String, Object> getConfiguration();
    void configure(Map<String, Object> config) throws PluginException;
}

// src/main/java/com/example/generator/plugin/TemplateProcessor.java
package com.example.generator.plugin;

import java.util.Map;

public interface TemplateProcessor {
    
    String getTemplateType();
    
    void initialize() throws PluginException;
    
    boolean supports(String templateType);
    
    String processTemplate(String templateContent, Map<String, Object> data, PluginExecutionContext context) 
            throws PluginException;
    
    void addCustomFunction(String name, TemplateFunction function);
}

// src/main/java/com/example/generator/plugin/SpecificationProcessor.java
package com.example.generator.plugin;

import com.example.generator.ApiSpecification;
import java.io.InputStream;

public interface SpecificationProcessor {
    
    String getSpecificationType();
    
    void initialize() throws PluginException;
    
    boolean canProcess(String content, String filename);
    
    ApiSpecification processSpecification(InputStream content, String filename, 
                                        PluginExecutionContext context) throws PluginException;
    
    ValidationResult validateSpecification(InputStream content, String filename,
                                         PluginExecutionContext context) throws PluginException;
}

// src/main/java/com/example/generator/plugin/ValidationPlugin.java
package com.example.generator.plugin;

import java.util.List;

public interface ValidationPlugin {
    
    String getValidationType();
    
    void initialize() throws PluginException;
    
    boolean supports(String validationType);
    
    ValidationResult validate(Object target, PluginExecutionContext context) throws PluginException;
    
    List<ValidationRule> getValidationRules();
}

// src/main/java/com/example/generator/plugin/PluginExecutionContext.java
package com.example.generator.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginExecutionContext {
    
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, Object> configuration = new HashMap<>();
    private final long startTime = System.currentTimeMillis();
    private String executionId = java.util.UUID.randomUUID().toString();
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        return (T) attributes.get(key);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public void setConfiguration(Map<String, Object> config) {
        this.configuration.clear();
        this.configuration.putAll(config);
    }
    
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public long getExecutionTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public void log(String level, String message, Object... args) {
        System.out.printf("[%s] [%s] [%s] %s%n", 
            level, executionId, Thread.currentThread().getName(), 
            String.format(message, args));
    }
}

// src/main/java/com/example/generator/plugin/PluginResult.java
package com.example.generator.plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginResult {
    
    private final boolean success;
    private final String message;
    private final Map<String, Object> data = new HashMap<>();
    private final Exception error;
    
    private PluginResult(boolean success, String message, Exception error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }
    
    public static PluginResult success(String message) {
        return new PluginResult(true, message, null);
    }
    
    public static PluginResult failure(String message, Exception error) {
        return new PluginResult(false, message, error);
    }
    
    public PluginResult withData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Map<String, Object> getData() { return new HashMap<>(data); }
    public Exception getError() { return error; }
}

// src/main/java/com/example/generator/plugin/PluginException.java
package com.example.generator.plugin;

public class PluginException extends Exception {
    
    private final String pluginId;
    private final String operation;
    
    public PluginException(String pluginId, String operation, String message) {
        super(String.format("[%s:%s] %s", pluginId, operation, message));
        this.pluginId = pluginId;
        this.operation = operation;
    }
    
    public PluginException(String pluginId, String operation, String message, Throwable cause) {
        super(String.format("[%s:%s] %s", pluginId, operation, message), cause);
        this.pluginId = pluginId;
        this.operation = operation;
    }
    
    public String getPluginId() { return pluginId; }
    public String getOperation() { return operation; }
}

// src/main/java/com/example/generator/plugin/TemplateFunction.java
package com.example.generator.plugin;

@FunctionalInterface
public interface TemplateFunction {
    Object apply(Object... args) throws PluginException;
}

// src/main/java/com/example/generator/plugin/ValidationRule.java
package com.example.generator.plugin;

public class ValidationRule {
    
    private final String name;
    private final String description;
    private final ValidationSeverity severity;
    private final ValidationFunction validator;
    
    public ValidationRule(String name, String description, ValidationSeverity severity, ValidationFunction validator) {
        this.name = name;
        this.description = description;
        this.severity = severity;
        this.validator = validator;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ValidationSeverity getSeverity() { return severity; }
    public ValidationFunction getValidator() { return validator; }
    
    public enum ValidationSeverity {
        ERROR, WARNING, INFO
    }
    
    @FunctionalInterface
    public interface ValidationFunction {
        ValidationResult validate(Object target, PluginExecutionContext context);
    }
}

// src/main/java/com/example/generator/plugin/ValidationResult.java
package com.example.generator.plugin;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    private final boolean valid;
    private final List<ValidationIssue> issues = new ArrayList<>();
    
    public ValidationResult(boolean valid) {
        this.valid = valid;
    }
    
    public ValidationResult addIssue(ValidationIssue issue) {
        this.issues.add(issue);
        return this;
    }
    
    public ValidationResult addError(String message) {
        return addIssue(new ValidationIssue(ValidationRule.ValidationSeverity.ERROR, message));
    }
    
    public ValidationResult addWarning(String message) {
        return addIssue(new ValidationIssue(ValidationRule.ValidationSeverity.WARNING, message));
    }
    
    public boolean isValid() { return valid; }
    public List<ValidationIssue> getIssues() { return new ArrayList<>(issues); }
    
    public List<ValidationIssue> getErrors() {
        return issues.stream()
            .filter(issue -> issue.getSeverity() == ValidationRule.ValidationSeverity.ERROR)
            .toList();
    }
    
    public List<ValidationIssue> getWarnings() {
        return issues.stream()
            .filter(issue -> issue.getSeverity() == ValidationRule.ValidationSeverity.WARNING)
            .toList();
    }
    
    public static class ValidationIssue {
        private final ValidationRule.ValidationSeverity severity;
        private final String message;
        private final String field;
        
        public ValidationIssue(ValidationRule.ValidationSeverity severity, String message) {
            this(severity, message, null);
        }
        
        public ValidationIssue(ValidationRule.ValidationSeverity severity, String message, String field) {
            this.severity = severity;
            this.message = message;
            this.field = field;
        }
        
        public ValidationRule.ValidationSeverity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getField() { return field; }
    }
}

// src/main/java/com/example/generator/plugin/builtin/DefaultTemplateProcessor.java
package com.example.generator.plugin.builtin;

import com.example.generator.plugin.*;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DefaultTemplateProcessor implements TemplateProcessor {
    
    @Inject
    Engine quteEngine;
    
    private final Map<String, TemplateFunction> customFunctions = new ConcurrentHashMap<>();
    
    @Override
    public String getTemplateType() {
        return "qute";
    }
    
    @Override
    public void initialize() throws PluginException {
        // Add built-in template functions
        addCustomFunction("camelCase", args -> {
            if (args.length > 0 && args[0] != null) {
                return toCamelCase(args[0].toString());
            }
            return "";
        });
        
        addCustomFunction("upperCase", args -> {
            if (args.length > 0 && args[0] != null) {
                return args[0].toString().toUpperCase();
            }
            return "";
        });
        
        addCustomFunction("lowerCase", args -> {
            if (args.length > 0 && args[0] != null) {
                return args[0].toString().toLowerCase();
            }
            return "";
        });
        
        addCustomFunction("sanitize", args -> {
            if (args.length > 0 && args[0] != null) {
                return args[0].toString().replaceAll("[^a-zA-Z0-9_]", "_");
            }
            return "";
        });
    }
    
    @Override
    public boolean supports(String templateType) {
        return "qute".equalsIgnoreCase(templateType) || 
               "default".equalsIgnoreCase(templateType);
    }
    
    @Override
    public String processTemplate(String templateContent, Map<String, Object> data, 
                                PluginExecutionContext context) throws PluginException {
        try {
            Template template = quteEngine.parse(templateContent);
            
            // Add custom functions to data context
            Map<String, Object> enrichedData = new java.util.HashMap<>(data);
            enrichedData.putAll(customFunctions);
            
            return template.data(enrichedData).render();
            
        } catch (Exception e) {
            throw new PluginException("default-template-processor", "process", 
                "Failed to process template", e);
        }
    }
    
    @Override
    public void addCustomFunction(String name, TemplateFunction function) {
        customFunctions.put(name, function);
    }
    
    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) return input;
        
        String[] parts = input.split("[_\\-\\s]+");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(parts[i].substring(0, 1).toUpperCase())
                      .append(parts[i].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
}

// src/main/java/com/example/generator/plugin/builtin/OpenApiValidationPlugin.java
package com.example.generator.plugin.builtin;

import com.example.generator.plugin.*;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class OpenApiValidationPlugin implements ValidationPlugin {
    
    @Override
    public String getValidationType() {
        return "openapi";
    }
    
    @Override
    public void initialize() throws PluginException {
        // Initialization logic
    }
    
    @Override
    public boolean supports(String validationType) {
        return "openapi".equalsIgnoreCase(validationType) || 
               "swagger".equalsIgnoreCase(validationType);
    }
    
    @Override
    public ValidationResult validate(Object target, PluginExecutionContext context) throws PluginException {
        if (!(target instanceof OpenAPI)) {
            return new ValidationResult(false).addError("Target is not an OpenAPI specification");
        }
        
        OpenAPI openAPI = (OpenAPI) target;
        ValidationResult result = new ValidationResult(true);
        
        // Validate info section
        if (openAPI.getInfo() == null) {
            result.addError("OpenAPI specification must have an 'info' section");
        } else {
            if (openAPI.getInfo().getTitle() == null || openAPI.getInfo().getTitle().trim().isEmpty()) {
                result.addWarning("OpenAPI info.title is missing or empty");
            }
            if (openAPI.getInfo().getVersion() == null || openAPI.getInfo().getVersion().trim().isEmpty()) {
                result.addWarning("OpenAPI info.version is missing or empty");
            }
        }
        
        // Validate paths
        if (openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            result.addError("OpenAPI specification must have at least one path defined");
        }
        
        return result;
    }
    
    @Override
    public List<ValidationRule> getValidationRules() {
        return Arrays.asList(
            new ValidationRule("info-required", "Info section is required", 
                ValidationRule.ValidationSeverity.ERROR, this::validateInfo),
            new ValidationRule("paths-required", "At least one path is required", 
                ValidationRule.ValidationSeverity.ERROR, this::validatePaths),
            new ValidationRule("title-recommended", "Title should be provided", 
                ValidationRule.ValidationSeverity.WARNING, this::validateTitle)
        );
    }
    
    private ValidationResult validateInfo(Object target, PluginExecutionContext context) {
        OpenAPI openAPI = (OpenAPI) target;
        ValidationResult result = new ValidationResult(openAPI.getInfo() != null);
        if (openAPI.getInfo() == null) {
            result.addError("Info section is required");
        }
        return result;
    }
    
    private ValidationResult validatePaths(Object target, PluginExecutionContext context) {
        OpenAPI openAPI = (OpenAPI) target;
        boolean hasPaths = openAPI.getPaths() != null && !openAPI.getPaths().isEmpty();
        ValidationResult result = new ValidationResult(hasPaths);
        if (!hasPaths) {
            result.addError("At least one path must be defined");
        }
        return result;
    }
    
    private ValidationResult validateTitle(Object target, PluginExecutionContext context) {
        OpenAPI openAPI = (OpenAPI) target;
        ValidationResult result = new ValidationResult(true);
        if (openAPI.getInfo() != null && 
            (openAPI.getInfo().getTitle() == null || openAPI.getInfo().getTitle().trim().isEmpty())) {
            result.addWarning("API title should be provided");
        }
        return result;
    }
}