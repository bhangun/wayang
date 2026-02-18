package tech.kayys.wayang.control.dto;

import java.util.List;
import java.util.Map;

// Removed broken imports

public class Guardrail {
    public String type;
    public boolean enabled;

    // Stubbed fields/constructors if used elsewhere
    // Assuming simple DTO usage

    public Guardrail() {
    }

    /*
     * public GuardrailType type;
     * public boolean enabled;
     * public GuardrailConfig config;
     */

    // Use String for now to avoid enum dependency issues if GuardrailType is
    // missing
}
