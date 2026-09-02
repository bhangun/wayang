package tech.kayys.wayang.agent.core.core;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.agent.spi.AgentRequest;
import tech.kayys.wayang.agent.spi.AgentResponse;

public interface AgentClient {
    Uni<AgentResponse> execute(AgentRequest request);
}
