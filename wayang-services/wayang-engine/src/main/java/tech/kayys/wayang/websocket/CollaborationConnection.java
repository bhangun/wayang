package tech.kayys.wayang.websocket;

import java.util.Map;

import org.jboss.logging.Logger;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocketClient;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import tech.kayys.wayang.schema.CollaborationEvent;
import tech.kayys.wayang.schema.CollaborationMessage;
import tech.kayys.wayang.schema.EventType;
import tech.kayys.wayang.utils.JsonUtils;

/**
 * CollaborationConnection - Represents an active WebSocket connection
 * Handlers are retrieved from CollaborationHandlerRegistry using 'handlerId'
 * query param
 */
@WebSocketClient(path = "/ws/workflows/{workflowId}")
public class CollaborationConnection {

    private static final Logger LOG = Logger.getLogger(CollaborationConnection.class);

    @Inject
    CollaborationHandlerRegistry registry;

    private String workflowId;
    private String userId;
    private String tenantId;
    private CollaborationHandler handler;

    private WebSocketConnection connection;
    private boolean connected = false;

    public CollaborationConnection() {
    }

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        this.connection = connection;
        this.connected = true;

        this.workflowId = connection.pathParam("workflowId");
        String handlerId = connection.handshakeRequest().queryParam("handlerId");
        this.userId = connection.handshakeRequest().queryParam("userId");
        this.tenantId = connection.handshakeRequest().queryParam("tenantId");

        if (handlerId != null) {
            this.handler = registry.get(handlerId);
        }

        LOG.infof("WS connected: workflow=%s, user=%s, handler=%s",
                workflowId, userId, (handler != null ? "found" : "not found"));

        if (handler != null) {
            handler.onConnected(workflowId);
        }
    }

    @OnTextMessage
    public void onMessage(String message) {
        LOG.debugf("WS message received: workflow=%s", workflowId);

        if (handler == null)
            return;

        try {
            CollaborationEvent event = JsonUtils.fromJson(message,
                    CollaborationEvent.class);

            // Route event to handler
            switch (event.getType()) {
                case USER_JOINED -> handler.onUserJoined(event);
                case USER_LEFT -> handler.onUserLeft(event);
                case CURSOR_MOVED -> handler.onCursorMoved(event);
                case NODE_MOVED -> handler.onNodeMoved(event);
                case NODE_LOCKED -> handler.onNodeLocked(event);
                case NODE_UNLOCKED -> handler.onNodeUnlocked(event);
                case NODE_UPDATED -> handler.onNodeUpdated(event);
                case CONNECTION_ADDED -> handler.onConnectionAdded(event);
                case CONNECTION_DELETED -> handler.onConnectionDeleted(event);
                case SELECTION_CHANGED -> handler.onSelectionChanged(event);
                case LOCK_FAILED -> handler.onLockFailed(event);
                case ERROR -> handler.onError(event);
                default -> LOG.warnf("Unknown event type: %s", event.getType());
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process WS message: %s", message);
            handler.onError(buildErrorEvent(e));
        }
    }

    @OnClose
    public void onClose() {
        this.connected = false;
        LOG.infof("WS disconnected: workflow=%s", workflowId);
        if (handler != null) {
            handler.onDisconnected(workflowId);
        }
    }

    @OnError
    public void onError(Throwable error) {
        LOG.errorf(error, "WS error: workflow=%s", workflowId);
        if (handler != null) {
            handler.onError(buildErrorEvent(error));
        }
    }

    /**
     * Send message
     */
    public Uni<Void> send(CollaborationMessage message) {
        if (!connected || connection == null) {
            return Uni.createFrom().failure(
                    new IllegalStateException("Not connected"));
        }

        String json = JsonUtils.toJson(message);
        connection.sendText(json);
        return Uni.createFrom().voidItem();
    }

    /**
     * Close connection
     */
    public Uni<Void> close() {
        if (connection != null) {
            connection.close();
        }
        return Uni.createFrom().voidItem();
    }

    private CollaborationEvent buildErrorEvent(Throwable error) {
        return CollaborationEvent.builder()
                .type(EventType.ERROR)
                .userId(userId)
                .workflowId(workflowId)
                .payload(Map.of("error", error.getMessage()))
                .build();
    }
}
