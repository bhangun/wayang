package tech.kayys.wayang.schema.validator;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Validator for plugin configuration schemas
 */
@ApplicationScoped
public class PluginConfigValidator {
    
    @Inject
    private SchemaValidationService validationService;
    
    /**
     * Validates a plugin configuration
     */
    public ValidationResult validatePluginConfig(Map<String, Object> config) {
        // Define validation rules specific to plugin configurations
        ValidationRule[] rules = {
            new ValidationRule("id", "required", null, "Plugin ID is required", "ERROR", true),
            new ValidationRule("name", "required", null, "Plugin name is required", "ERROR", true),
            new ValidationRule("version", "required", null, "Plugin version is required", "ERROR", true),
            new ValidationRule("id", "pattern", "^[a-zA-Z][a-zA-Z0-9_-]*$", "Plugin ID must start with a letter and contain only letters, numbers, hyphens, and underscores", "ERROR", false)
        };
        
        // Basic schema validation
        String schema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"id\": { \"type\": \"string\", \"pattern\": \"^[a-zA-Z][a-zA-Z0-9_-]*$\" },\n" +
                "    \"name\": { \"type\": \"string\" },\n" +
                "    \"version\": { \"type\": \"string\" },\n" +
                "    \"description\": { \"type\": \"string\" },\n" +
                "    \"provider\": { \"type\": \"string\" },\n" +
                "    \"capabilities\": { \n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": { \"type\": \"string\" }\n" +
                "    },\n" +
                "    \"configuration\": { \"type\": \"object\" }\n" +
                "  },\n" +
                "  \"required\": [\"id\", \"name\", \"version\"]\n" +
                "}";
        
        return validationService.validateComprehensive(schema, rules, config);
    }
}