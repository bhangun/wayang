package tech.kayys.wayang.a2a.descriptor;

import java.util.Map;

/**
 * A skill advertised in the A2A Agent Card — maps from a Wayang AgentCapability.
 */
public record A2ASkill(
        String id,
        String description,
        Map<String, Object> metadata
) {

    public A2ASkill {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static A2ASkill of(String id, String description) {
        return new A2ASkill(id, description, Map.of());
    }
}
