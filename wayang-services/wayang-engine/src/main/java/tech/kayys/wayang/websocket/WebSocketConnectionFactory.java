package tech.kayys.wayang.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.websockets.next.WebSocketConnector;
import io.smallrye.mutiny.Uni;
import java.util.UUID;

/**
 * Factory for creating WebSocket connections for collaboration
 */
@ApplicationScoped
public class WebSocketConnectionFactory {

    @Inject
    WebSocketConnector<CollaborationConnection> connector;

    @Inject
    CollaborationHandlerRegistry registry;

    public Uni<CollaborationConnection> connect(String url, String userId, String tenantId,
            CollaborationHandler handler) {

        String handlerId = UUID.randomUUID().toString();
        registry.register(handlerId, handler);

        // Append query params for handler resolution
        String wsUrl = url;
        if (wsUrl.contains("?")) {
            wsUrl += "&handlerId=" + handlerId;
        } else {
            wsUrl += "?handlerId=" + handlerId;
        }
        wsUrl += "&userId=" + userId + "&tenantId=" + tenantId;

        return connector.baseUri(wsUrl)
                .connect()
                .map(conn -> (CollaborationConnection) conn.receiver());
    }
}
