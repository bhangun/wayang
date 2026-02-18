package tech.kayys.wayang.control.dto;

import java.util.Map;

/**
 * Safety guardrail for an AI agent.
 */
public class Guardrail {
    public String name;
    public GuardrailType type;
    public String policy; // e.g., "deny", "warn", "filter"
    public Map<String, Object> configuration;
}
