package tech.kayys.wayang.integration.core.client;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.integration.core.config.EndpointConfig;

public interface EndpointClient {
    Uni<Object> send(EndpointConfig config, Object payload);
}
