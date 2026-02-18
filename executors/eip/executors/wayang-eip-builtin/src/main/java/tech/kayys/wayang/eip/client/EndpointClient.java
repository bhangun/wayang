package tech.kayys.wayang.eip.client;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.eip.config.EndpointConfig;

public interface EndpointClient {
    Uni<Object> send(EndpointConfig config, Object payload);
}
