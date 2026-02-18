package tech.kayys.wayang.schema.validator;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Validator for workflow schemas
 */
@ApplicationScoped
public class WorkflowValidator {
    
    @Inject
    private SchemaValidationService validationService;
    
    /**
     * Validates a workflow configuration
     */
    public ValidationResult validateWorkflow(Map<String, Object> workflow) {
        // Define validation rules specific to workflow configurations
        ValidationRule[] rules = {
            new ValidationRule("name", "required", null, "Workflow name is required", "ERROR", true),
            new ValidationRule("name", "length", "1,100", "Workflow name must be between 1 and 100 characters", "ERROR", false),
            new ValidationRule("nodes", "required", null, "Workflow must have at least one node", "ERROR", true)
        };
        
        // Basic schema validation
        String schema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"name\": { \"type\": \"string\" },\n" +
                "    \"description\": { \"type\": \"string\" },\n" +
                "    \"nodes\": { \n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": { \"$ref\": \"#/definitions/node\" }\n" +
                "    },\n" +
                "    \"connections\": { \n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": { \"$ref\": \"#/definitions/connection\" }\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"name\", \"nodes\"],\n" +
                "  \"definitions\": {\n" +
                "    \"node\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"id\": { \"type\": \"string\" },\n" +
                "        \"type\": { \"type\": \"string\" },\n" +
                "        \"configuration\": { \"type\": \"object\" }\n" +
                "      },\n" +
                "      \"required\": [\"id\", \"type\"]\n" +
                "    },\n" +
                "    \"connection\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"from\": { \"type\": \"string\" },\n" +
                "        \"to\": { \"type\": \"string\" }\n" +
                "      },\n" +
                "      \"required\": [\"from\", \"to\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        return validationService.validateComprehensive(schema, rules, workflow);
    }
}