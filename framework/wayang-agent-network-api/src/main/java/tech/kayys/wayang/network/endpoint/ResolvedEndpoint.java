package tech.kayys.wayang.network.endpoint;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public record ResolvedEndpoint(
        URI uri,
        String protocol,
        String version,
        Map<String, Object> metadata
) {

    public ResolvedEndpoint {
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(protocol, "protocol");
        version  = version == null ? "1.0" : version;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static ResolvedEndpoint of(URI uri, String protocol, String version) {
        return new ResolvedEndpoint(uri, protocol, version, Map.of());
    }
}
