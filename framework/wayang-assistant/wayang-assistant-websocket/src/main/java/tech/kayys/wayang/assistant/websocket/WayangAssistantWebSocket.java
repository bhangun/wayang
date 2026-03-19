package tech.kayys.wayang.assistant.websocket;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;
import tech.kayys.wayang.assistant.agent.WayangAssistantService;
import tech.kayys.gollek.spi.stream.StreamChunk;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for the Wayang Internal Assistant.
 * Provides real-time streaming chat capabilities.
 *
 * URL: ws://localhost:8080/api/v1/assistant/chat-stream
 */
@ServerEndpoint("/api/v1/assistant/chat-stream")
@ApplicationScoped
public class WayangAssistantWebSocket {

    private static final Logger LOG = Logger.getLogger(WayangAssistantWebSocket.class);
    private static final int MAX_ERROR_MSG_LENGTH = 500;

    @Inject
    WayangAssistantService assistantService;

    @Inject
    ObjectMapper objectMapper;

    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        LOG.infof("Assistant WebSocket opened: %s", session.getId());
        activeSessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        LOG.infof("Assistant WebSocket closed: %s", session.getId());
        activeSessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.errorf("Assistant WebSocket error for %s: %s", session.getId(), safeMessage(throwable));
        activeSessions.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOG.debugf("Received message from %s: %s", session.getId(), message);

        try {
            processMessage(message, session);
        } catch (Throwable t) {
            // Catch Throwable (not just Exception) to handle StackOverflowError, OOM, etc.
            LOG.errorf("Fatal error processing message for %s: %s", session.getId(), safeMessage(t));
            sendError(session, "Internal server error");
        }
    }

    private void processMessage(String message, Session session) {
        String userPrompt;
        String assistantSessionId;

        try {
            // Attempt to parse as JSON: { "sessionId": "...", "message": "..." }
            Map<String, String> payload = objectMapper.readValue(message, Map.class);
            userPrompt = payload.getOrDefault("message", payload.get("text"));
            assistantSessionId = payload.getOrDefault("sessionId", "ws-" + session.getId());
        } catch (Exception e) {
            // Fallback: use raw message
            userPrompt = message;
            assistantSessionId = "ws-" + session.getId();
        }

        if (userPrompt == null || userPrompt.isBlank()) {
            sendError(session, "Empty message received");
            return;
        }

        assistantService.chatStream(assistantSessionId, userPrompt)
                .subscribe().with(
                        chunk -> sendChunk(session, chunk),
                        failure -> handleError(session, failure),
                        () -> LOG.debugf("Streaming completed for session %s", session.getId())
                );
    }

    private void sendChunk(Session session, StreamChunk chunk) {
        if (session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(chunk.getDelta());
            } catch (Exception e) {
                LOG.error("Failed to send chunk", e);
            }
        }
    }

    private void handleError(Session session, Throwable failure) {
        LOG.error("Assistant streaming error", failure);
        sendError(session, "Internal error: " + safeMessage(failure));
    }

    private void sendError(Session session, String errorMsg) {
        if (session.isOpen()) {
            String truncated = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                    ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH) + "…"
                    : (errorMsg != null ? errorMsg : "Unknown error");
            // Escape quotes for JSON safety
            String escaped = truncated.replace("\\", "\\\\").replace("\"", "\\\"");
            session.getAsyncRemote().sendText("{\"error\": \"" + escaped + "\"}");
        }
    }

    /**
     * Safely extract a message from a Throwable without risking another StackOverflowError.
     */
    private static String safeMessage(Throwable t) {
        if (t == null) return "null";
        try {
            String msg = t.getMessage();
            if (msg != null && !msg.isBlank()) return msg;
            return t.getClass().getSimpleName();
        } catch (Throwable ignored) {
            // getMessage() itself can fail for certain Error subclasses
            return t.getClass().getSimpleName();
        }
    }
}
