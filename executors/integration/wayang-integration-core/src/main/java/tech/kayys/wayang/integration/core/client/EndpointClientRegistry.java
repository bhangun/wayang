package tech.kayys.wayang.integration.core.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Registry for EndpointClient implementations
 */
@ApplicationScoped
public class EndpointClientRegistry {

    @Inject
    @Any
    Instance<EndpointClient> clients;

    public EndpointClient getClient(String protocol) {
        // In a real implementation, clients would be mapped by protocol
        // For now, simple matching or default
        for (EndpointClient client : clients) {
            String clientName = client.getClass().getSimpleName().toLowerCase();
            if (clientName.startsWith(protocol.toLowerCase())) {
                return client;
            }
        }

        // Return first available client as fallback or throw
        if (!clients.isUnsatisfied()) {
            return clients.iterator().next();
        }

        throw new IllegalArgumentException("No EndpointClient found for protocol: " + protocol);
    }
}
