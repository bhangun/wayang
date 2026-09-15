package tech.kayys.wayang.anp.client;

import tech.kayys.wayang.anp.config.AnpProtocolConfig;
import tech.kayys.wayang.network.protocol.AgentNetworkClient;
import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;

import java.util.Objects;

/**
 * Implementation of {@link AgentNetworkProtocol} for the Agent Network Protocol (ANP) 1.1.
 *
 * <p>Registers in the {@code AgentNetworkProtocolRegistry} when ANP is enabled in configuration.
 */
public final class AnpNetworkProtocol implements AgentNetworkProtocol {

    public static final String PROTOCOL_ID = "anp";
    public static final String PROTOCOL_VERSION = "1.1";

    private final AnpNetworkClient client;
    private final AnpProtocolConfig config;

    public AnpNetworkProtocol(AnpNetworkClient client, AnpProtocolConfig config) {
        this.client = Objects.requireNonNull(client, "client");
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public String id() {
        return PROTOCOL_ID;
    }

    @Override
    public String version() {
        return PROTOCOL_VERSION;
    }

    @Override
    public AgentNetworkClient client() {
        return client;
    }

    public boolean isEnabled() {
        return config.enabled();
    }
}
