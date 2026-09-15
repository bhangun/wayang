package tech.kayys.wayang.network.core.local;

import tech.kayys.wayang.communication.api.AgentCommunicator;
import tech.kayys.wayang.network.protocol.AgentNetworkClient;
import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;

import java.util.Objects;

public final class LocalAgentNetworkProtocol implements AgentNetworkProtocol {

    private final AgentNetworkClient client;

    public LocalAgentNetworkProtocol(AgentCommunicator communicator) {
        Objects.requireNonNull(communicator, "communicator");
        this.client = new LocalAgentNetworkClient(communicator);
    }

    @Override
    public String id() {
        return "LOCAL";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public AgentNetworkClient client() {
        return client;
    }
}
