package tech.kayys.wayang.communication.endpoint;

import java.net.URI;
import java.util.Objects;

public record RemoteAgentEndpoint(
        URI uri
) implements AgentEndpoint {

    public RemoteAgentEndpoint {
        Objects.requireNonNull(uri, "uri");
    }

    public static RemoteAgentEndpoint of(URI uri) {
        return new RemoteAgentEndpoint(uri);
    }

    public static RemoteAgentEndpoint of(String uriString) {
        return new RemoteAgentEndpoint(URI.create(uriString));
    }

    @Override
    public EndpointType type() {
        return EndpointType.REMOTE;
    }
}
