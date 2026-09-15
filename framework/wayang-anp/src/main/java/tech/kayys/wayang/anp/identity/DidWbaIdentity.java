package tech.kayys.wayang.anp.identity;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parsed Decentralized Identifier according to the DID:WBA (Web-Based Agent) specification (ANP 1.1).
 *
 * <p>Format: {@code did:wba:<hostname>[:<port>][:<path-segment1>:<path-segment2>...]}
 * Examples:
 * <ul>
 *   <li>{@code did:wba:example.com}</li>
 *   <li>{@code did:wba:example.com:agent1}</li>
 *   <li>{@code did:wba:agents.kayys.tech:finance:advisor}</li>
 * </ul>
 */
public record DidWbaIdentity(
        String raw,
        String host,
        int port,
        List<String> pathSegments
) {

    public static final String PREFIX = "did:wba:";

    public DidWbaIdentity {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(host, "host");
        pathSegments = pathSegments == null ? List.of() : List.copyOf(pathSegments);
    }

    /** Parses a DID:WBA string. */
    public static DidWbaIdentity parse(String did) {
        Objects.requireNonNull(did, "did string must not be null");
        if (!did.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid DID:WBA identifier, must start with 'did:wba:': " + did);
        }

        String remainder = did.substring(PREFIX.length());
        String[] parts = remainder.split(":");
        if (parts.length == 0 || parts[0].isBlank()) {
            throw new IllegalArgumentException("Invalid DID:WBA: missing host in " + did);
        }

        String hostPart = parts[0];
        int portPart = -1;
        int nextIndex = 1;

        if (parts.length > 1 && parts[1].matches("\\d+")) {
            portPart = Integer.parseInt(parts[1]);
            nextIndex = 2;
        }

        List<String> segments = nextIndex < parts.length
                ? Arrays.asList(Arrays.copyOfRange(parts, nextIndex, parts.length))
                : List.of();

        return new DidWbaIdentity(did, hostPart, portPart, segments);
    }

    /** Creates a DID:WBA identity for a given host and agent name. */
    public static DidWbaIdentity of(String host, String agentName) {
        String raw = (agentName == null || agentName.isBlank())
                ? PREFIX + host
                : PREFIX + host + ":" + agentName;
        return parse(raw);
    }

    /** Returns the HTTPS URL for resolving this agent's DID document. */
    public URI toDidDocumentUrl() {
        StringBuilder sb = new StringBuilder("https://").append(host);
        if (port > 0) {
            sb.append(":").append(port);
        }
        if (pathSegments.isEmpty()) {
            sb.append("/.well-known/did.json");
        } else {
            for (String segment : pathSegments) {
                sb.append("/").append(segment);
            }
            sb.append("/did.json");
        }
        return URI.create(sb.toString());
    }

    /** Returns the HTTPS URL for this agent's Agent Description document (/.well-known/agent.json). */
    public URI toAgentDescriptionUrl() {
        StringBuilder sb = new StringBuilder("https://").append(host);
        if (port > 0) {
            sb.append(":").append(port);
        }
        sb.append("/.well-known/agent.json");
        return URI.create(sb.toString());
    }

    @Override
    public String toString() {
        return raw;
    }
}
