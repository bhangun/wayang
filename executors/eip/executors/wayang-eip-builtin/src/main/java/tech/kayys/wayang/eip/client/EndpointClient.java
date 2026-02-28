package tech.kayys.wayang.eip.client;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.eip.dto.EndpointDto;

public interface EndpointClient {
    Uni<Object> send(EndpointDto config, Object payload);
}
