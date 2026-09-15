package tech.kayys.wayang.network.protocol;

public interface AgentNetworkProtocol {

    String id();

    String version();

    AgentNetworkClient client();

    default AgentNetworkServer server() {
        return null;
    }
}
