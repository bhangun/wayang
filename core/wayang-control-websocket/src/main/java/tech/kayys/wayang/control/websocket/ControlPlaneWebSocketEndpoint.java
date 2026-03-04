package tech.kayys.wayang.control.websocket;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.schema.validator.ValidationResult;

/**
 * Control-plane WebSocket endpoint for realtime front-end interoperability.
 *
 * Supported message types:
 * - ping: health ping/pong
 * - validate: validate payload against schemaId/channel mapping
 * - publish: validate (if schemaId/payload are present) and broadcast event to workspace peers
 */
@ServerEndpoint(value = "/ws/v1/control-plane/{workspaceId}", configurator = ControlPlaneHandshakeConfigurator.class)
public class ControlPlaneWebSocketEndpoint {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ControlPlaneWebSocketBroker broker;

    @Inject
    SchemaValidationService schemaValidationService;

    @Inject
    ControlPlaneSchemaRouter schemaRouter;

    @OnOpen
    public void onOpen(Session session, @PathParam("workspaceId") String workspaceId) {
        if (!authorizeSession(session, workspaceId)) {
            return;
        }
        broker.join(workspaceId, session);
        sendSafe(session, asJson(Map.of(
                "type", "session.opened",
                "workspaceId", workspaceId,
                "sessionId", session.getId(),
                "participants", broker.roomSize(workspaceId),
                "timestamp", Instant.now().toString())));
    }

    @OnClose
    public void onClose(Session session, @PathParam("workspaceId") String workspaceId, CloseReason reason) {
        broker.leave(workspaceId, session);
        broker.broadcast(workspaceId, asJson(Map.of(
                "type", "session.closed",
                "workspaceId", workspaceId,
                "sessionId", session.getId(),
                "participants", broker.roomSize(workspaceId),
                "reason", reason != null ? reason.getReasonPhrase() : "closed",
                "timestamp", Instant.now().toString())), session);
    }

    @OnError
    public void onError(Session session, @PathParam("workspaceId") String workspaceId, Throwable throwable) {
        sendSafe(session, asJson(Map.of(
                "type", "error",
                "workspaceId", workspaceId,
                "sessionId", session != null ? session.getId() : "unknown",
                "message", throwable != null ? throwable.getMessage() : "Unknown websocket error",
                "timestamp", Instant.now().toString())));
    }

    @OnMessage
    public void onMessage(String rawMessage, Session session, @PathParam("workspaceId") String workspaceId) {
        try {
            ControlPlaneSocketMessage incoming = objectMapper.readValue(rawMessage, ControlPlaneSocketMessage.class);
            String type = normalizeType(incoming.type);

            if ("ping".equals(type)) {
                sendSafe(session, asJson(Map.of(
                        "type", "pong",
                        "correlationId", emptySafe(incoming.correlationId),
                        "timestamp", Instant.now().toString())));
                return;
            }

            if ("validate".equals(type)) {
                handleValidate(session, incoming);
                return;
            }

            if ("publish".equals(type)) {
                handlePublish(session, workspaceId, incoming);
                return;
            }

            sendSafe(session, asJson(Map.of(
                    "type", "error",
                    "correlationId", emptySafe(incoming.correlationId),
                    "message", "Unsupported message type: " + type,
                    "supportedTypes", new String[] {"ping", "validate", "publish"},
                    "timestamp", Instant.now().toString())));
        } catch (Exception e) {
            sendSafe(session, asJson(Map.of(
                    "type", "error",
                    "message", "Invalid websocket payload: " + e.getMessage(),
                    "timestamp", Instant.now().toString())));
        }
    }

    private void handleValidate(Session session, ControlPlaneSocketMessage incoming) {
        String schemaId = resolveSchemaId(incoming.channel, incoming.schemaId);
        if (schemaId == null) {
            sendSafe(session, asJson(Map.of(
                    "type", "validation.result",
                    "correlationId", emptySafe(incoming.correlationId),
                    "channel", emptySafe(incoming.channel),
                    "valid", false,
                    "message", "Invalid schema/channel mapping. channel=" + emptySafe(incoming.channel) + ", schemaId="
                            + emptySafe(incoming.schemaId),
                    "availableSchemas", schemaRouter.availableSchemas(),
                    "timestamp", Instant.now().toString())));
            return;
        }

        String schema = schemaRouterSchema(schemaId);
        Map<String, Object> payload = incoming.payload == null ? Map.of() : incoming.payload;
        ValidationResult result = schemaValidationService.validateSchema(schema, payload);
        sendSafe(session, asJson(Map.of(
                "type", "validation.result",
                "correlationId", emptySafe(incoming.correlationId),
                "schemaId", schemaId,
                "valid", result.isValid(),
                "message", result.getMessage(),
                "timestamp", Instant.now().toString())));
    }

    private void handlePublish(Session session, String workspaceId, ControlPlaneSocketMessage incoming) {
        String schemaId = resolveSchemaId(incoming.channel, incoming.schemaId);
        if (schemaId == null) {
            sendSafe(session, asJson(Map.of(
                    "type", "error",
                    "correlationId", emptySafe(incoming.correlationId),
                    "message", "Invalid schema/channel mapping. channel=" + emptySafe(incoming.channel) + ", schemaId="
                            + emptySafe(incoming.schemaId),
                    "availableSchemas", schemaRouter.availableSchemas(),
                    "timestamp", Instant.now().toString())));
            return;
        }

        if (incoming.payload != null) {
            String schema = schemaRouterSchema(schemaId);
            ValidationResult result = schemaValidationService.validateSchema(schema, incoming.payload);
            if (!result.isValid()) {
                sendSafe(session, asJson(Map.of(
                        "type", "validation.result",
                        "correlationId", emptySafe(incoming.correlationId),
                        "schemaId", schemaId,
                        "valid", false,
                        "message", result.getMessage(),
                        "timestamp", Instant.now().toString())));
                return;
            }
        }

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "workflow.event");
        event.put("correlationId", emptySafe(incoming.correlationId));
        event.put("workspaceId", workspaceId);
        event.put("channel", emptySafe(incoming.channel));
        event.put("schemaId", schemaId);
        event.put("payload", incoming.payload == null ? Map.of() : incoming.payload);
        event.put("metadata", incoming.metadata == null ? Map.of() : incoming.metadata);
        event.put("timestamp", Instant.now().toString());
        event.put("fromSessionId", session.getId());

        String json = asJson(event);
        broker.broadcast(workspaceId, json, session);
        sendSafe(session, asJson(Map.of(
                "type", "publish.ack",
                "correlationId", emptySafe(incoming.correlationId),
                "workspaceId", workspaceId,
                "timestamp", Instant.now().toString())));
    }

    private boolean authorizeSession(Session session, String workspaceId) {
        boolean enforce = ConfigProvider.getConfig().getOptionalValue("wayang.websocket.auth.enforce", Boolean.class)
                .orElse(Boolean.FALSE);
        if (!enforce) {
            return true;
        }

        boolean requireBearer = ConfigProvider.getConfig()
                .getOptionalValue("wayang.websocket.auth.require-bearer", Boolean.class).orElse(Boolean.TRUE);
        boolean requireTenant = ConfigProvider.getConfig()
                .getOptionalValue("wayang.websocket.auth.require-tenant", Boolean.class).orElse(Boolean.TRUE);

        String auth = asString(session.getUserProperties().get(ControlPlaneHandshakeConfigurator.AUTHORIZATION_PROP));
        String tenant = asString(session.getUserProperties().get(ControlPlaneHandshakeConfigurator.TENANT_ID_PROP));

        if (requireBearer && (auth == null || !auth.startsWith("Bearer "))) {
            closeUnauthorized(session, "Missing Bearer token");
            return false;
        }
        if (requireTenant && (tenant == null || tenant.isBlank())) {
            closeUnauthorized(session, "Missing X-Tenant-Id");
            return false;
        }
        if (requireTenant && workspaceId != null && workspaceId.startsWith("tenant:")) {
            String expectedTenant = workspaceId.substring("tenant:".length()).trim();
            if (!expectedTenant.isEmpty() && !expectedTenant.equalsIgnoreCase(tenant)) {
                closeUnauthorized(session, "Tenant mismatch");
                return false;
            }
        }

        return true;
    }

    private void closeUnauthorized(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException ignored) {
            // ignored
        }
    }

    private String resolveSchemaId(String channel, String schemaId) {
        return schemaRouter.resolveSchemaId(channel, schemaId);
    }

    private String schemaRouterSchema(String schemaId) {
        return tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog.get(schemaId);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toLowerCase();
    }

    private String emptySafe(String value) {
        return value == null ? "" : value;
    }

    private String asJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"Serialization failure\"}";
        }
    }

    private void sendSafe(Session session, String payload) {
        Objects.requireNonNull(payload, "payload must not be null");
        try {
            broker.send(session, payload);
        } catch (IOException ignored) {
            // Client may have disconnected while sending.
        }
    }
}
