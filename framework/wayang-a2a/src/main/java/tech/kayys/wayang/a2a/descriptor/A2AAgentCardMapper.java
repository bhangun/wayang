package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentDescriptor;
import tech.kayys.wayang.communication.api.AgentEndpointDescriptor;

import java.util.List;
import java.util.Objects;

/**
 * Pure mapper: projects a canonical {@link AgentDescriptor} into an A2A 1.0 {@link A2AAgentCard}.
 * <p>
 * This is the only place where A2A agent card structure is assembled.
 * No A2A concepts are allowed to flow upstream into AgentDescriptor.
 */
public final class A2AAgentCardMapper {

    private A2AAgentCardMapper() {}

    public static A2AAgentCard fromWayang(
            AgentDescriptor descriptor,
            String publicUrl
    ) {
        return fromWayang(descriptor, publicUrl, new DefaultA2ACapabilityMapper(), false);
    }

    public static A2AAgentCard fromWayang(
            AgentDescriptor descriptor,
            String publicUrl,
            A2ACapabilityMapper capabilityMapper,
            boolean exposeMetadata
    ) {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(capabilityMapper, "capabilityMapper");

        List<A2AInterface> interfaces = createInterfaces(descriptor, publicUrl);
        List<A2ASkill> skills = descriptor.capabilities()
                .stream()
                .map(capabilityMapper::map)
                .toList();

        List<String> inputModes  = resolveModes(descriptor, "inputModes",  List.of("text/plain", "application/json"));
        List<String> outputModes = resolveModes(descriptor, "outputModes", List.of("text/plain", "application/json"));

        return new A2AAgentCard(
                "1.0",
                descriptor.ref().name(),
                descriptor.description(),
                resolveVersion(descriptor),
                interfaces,
                A2ACapabilities.defaults(),
                skills,
                inputModes,
                outputModes,
                A2AMetadataPolicy.expose(descriptor, exposeMetadata)
        );
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private static List<A2AInterface> createInterfaces(AgentDescriptor descriptor, String publicUrl) {
        var bindingMapper = new DefaultA2AEndpointBindingMapper();

        // If there are declared endpoints, map each one that can be bound.
        List<A2AInterface> fromEndpoints = descriptor.endpoints()
                .stream()
                .filter(bindingMapper::supports)
                .map(bindingMapper::map)
                .toList();

        if (!fromEndpoints.isEmpty()) {
            return fromEndpoints;
        }

        // Fall back to the supplied publicUrl, if any.
        if (publicUrl != null && !publicUrl.isBlank()) {
            return List.of(A2AInterface.of(publicUrl, "HTTP+JSON", "1.0"));
        }

        return List.of();
    }

    private static String resolveVersion(AgentDescriptor descriptor) {
        Object v = descriptor.metadata().get("version");
        return v == null ? "1.0.0" : String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    private static List<String> resolveModes(
            AgentDescriptor descriptor,
            String key,
            List<String> defaults
    ) {
        Object value = descriptor.metadata().get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return defaults;
    }
}
