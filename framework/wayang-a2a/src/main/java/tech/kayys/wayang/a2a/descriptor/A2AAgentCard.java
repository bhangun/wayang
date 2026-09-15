package tech.kayys.wayang.a2a.descriptor;

import java.util.List;
import java.util.Map;

/**
 * The A2A 1.0 Agent Card — a protocol-specific projection of the canonical AgentDescriptor.
 * Served at {@code /.well-known/agent-card.json}.
 */
public record A2AAgentCard(
        String protocolVersion,
        String name,
        String description,
        String version,
        List<A2AInterface> supportedInterfaces,
        A2ACapabilities capabilities,
        List<A2ASkill> skills,
        List<String> inputModes,
        List<String> outputModes,
        Map<String, Object> metadata
) {

    public A2AAgentCard {
        supportedInterfaces = supportedInterfaces == null ? List.of() : List.copyOf(supportedInterfaces);
        skills               = skills == null               ? List.of() : List.copyOf(skills);
        inputModes           = inputModes == null           ? List.of() : List.copyOf(inputModes);
        outputModes          = outputModes == null          ? List.of() : List.copyOf(outputModes);
        metadata             = metadata == null             ? Map.of()  : Map.copyOf(metadata);
    }
}
