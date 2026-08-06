package tech.kayys.wayang.validation;

import tech.kayys.wayang.definition.AgentDefinition;

/**
 * Specific Validators
 */
public class AgentValidator implements Validator<AgentDefinition> {
    
    @Override
    public ValidationResult validate(AgentDefinition agent) {
        ValidationResult result = ValidationResult.success();
        
        if (agent.metadata().name() == null || agent.metadata().name().isEmpty()) {
            result = result.withError(new ValidationError("AGENT-001", "Agent name is required", "metadata.name"));
        }
        
        if (agent.role() == null || agent.role().isEmpty()) {
            result = result.withError(new ValidationError("AGENT-002", "Agent role is required", "role"));
        }
        
        if (agent.goal() == null || agent.goal().isEmpty()) {
            result = result.withError(new ValidationError("AGENT-003", "Agent goal is required", "goal"));
        }
        
        return result;
    }
}
