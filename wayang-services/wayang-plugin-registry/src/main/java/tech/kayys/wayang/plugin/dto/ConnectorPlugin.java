package tech.kayys.wayang.plugin.dto;

import io.smallrye.mutiny.Uni;

public sealed interface ConnectorPlugin extends Plugin {

    ConnectionType getConnectionType();

    Uni<Connection> connect(ConnectionConfig config);

    Uni<Void> disconnect();

    default Uni<Boolean> validateConfig(ConnectionConfig config) {
        return Uni.createFrom().item(config != null && config.isValid());
    }
}
