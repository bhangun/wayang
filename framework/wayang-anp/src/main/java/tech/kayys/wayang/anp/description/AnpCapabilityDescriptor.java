package tech.kayys.wayang.anp.description;

import java.util.Map;
import java.util.Objects;

/**
 * Describes a capability offered by an ANP agent, as declared in its agent description.
 */
public record AnpCapabilityDescriptor(
        String id,
        String name,
        String description,
        Map<String, Object> inputSchema,
        Map<String, Object> outputSchema
) {
    public AnpCapabilityDescriptor {
        Objects.requireNonNull(id, "id");
        inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
        outputSchema = outputSchema == null ? Map.of() : Map.copyOf(outputSchema);
    }

    public static AnpCapabilityDescriptor of(String id, String name, String description) {
        return new AnpCapabilityDescriptor(id, name, description, Map.of(), Map.of());
    }
}
