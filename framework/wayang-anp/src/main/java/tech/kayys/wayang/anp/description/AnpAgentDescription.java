package tech.kayys.wayang.anp.description;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Agent Description Document according to ANP 1.1 Specification (Agent Description Protocol).
 *
 * <p>Exposed at {@code /.well-known/agent.json} for agent discovery.
 */
public record AnpAgentDescription(
        String did,
        String name,
        String description,
        String version,
        List<String> protocols,
        List<AnpCapabilityDescriptor> capabilities,
        Map<String, String> endpoints,
        Map<String, Object> metadata
) {
    public AnpAgentDescription {
        Objects.requireNonNull(did, "did");
        Objects.requireNonNull(name, "name");
        protocols = protocols == null ? List.of("anp/1.1", "a2a/1.0") : List.copyOf(protocols);
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        endpoints = endpoints == null ? Map.of() : Map.copyOf(endpoints);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean supportsProtocol(String protocol) {
        return protocols.stream().anyMatch(p -> p.equalsIgnoreCase(protocol));
    }

    public boolean hasCapability(String capabilityId) {
        return capabilities.stream().anyMatch(c -> c.id().equalsIgnoreCase(capabilityId));
    }

    public static Builder builder(String did, String name) {
        return new Builder(did, name);
    }

    public static final class Builder {
        private final String did;
        private final String name;
        private String description = "";
        private String version = "1.0.0";
        private List<String> protocols = List.of("anp/1.1", "a2a/1.0");
        private List<AnpCapabilityDescriptor> capabilities = List.of();
        private Map<String, String> endpoints = Map.of();
        private Map<String, Object> metadata = Map.of();

        private Builder(String did, String name) {
            this.did = did;
            this.name = name;
        }

        public Builder description(String d) { this.description = d; return this; }
        public Builder version(String v) { this.version = v; return this; }
        public Builder protocols(List<String> p) { this.protocols = p; return this; }
        public Builder capabilities(List<AnpCapabilityDescriptor> c) { this.capabilities = c; return this; }
        public Builder endpoints(Map<String, String> e) { this.endpoints = e; return this; }
        public Builder metadata(Map<String, Object> m) { this.metadata = m; return this; }

        public AnpAgentDescription build() {
            return new AnpAgentDescription(did, name, description, version, protocols, capabilities, endpoints, metadata);
        }
    }
}
