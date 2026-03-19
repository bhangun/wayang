package tech.kayys.wayang.assistant.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the in-memory conversation history for a single assistant session.
 * Each session is identified by a caller-supplied sessionId string.
 */
public class ConversationSession {

    /** Maximum number of messages kept in memory per session. */
    private static final int MAX_HISTORY = 40;

    private final String sessionId;
    private final Instant createdAt;
    private Instant lastAccessedAt;
    private final List<ConversationMessage> messages = new ArrayList<>();

    public ConversationSession(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = Instant.now();
        this.lastAccessedAt = Instant.now();
    }

    /** Append a user turn to the history. */
    public synchronized void addUserMessage(String content) {
        addMessage(ConversationMessage.Role.USER, content);
    }

    /** Append an assistant turn to the history. */
    public synchronized void addAssistantMessage(String content) {
        addMessage(ConversationMessage.Role.ASSISTANT, content);
    }

    private void addMessage(ConversationMessage.Role role, String content) {
        if (messages.size() >= MAX_HISTORY) {
            // evict the two oldest messages (one user + one assistant)
            messages.remove(0);
            if (!messages.isEmpty()) messages.remove(0);
        }
        messages.add(new ConversationMessage(role, content, Instant.now()));
        lastAccessedAt = Instant.now();
    }

    /** Return an unmodifiable snapshot of the history. */
    public synchronized List<ConversationMessage> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    /** Return the last N user/assistant exchanges as plain-text context. */
    public synchronized String buildContextString(int lastNMessages) {
        List<ConversationMessage> recent = messages.subList(
                Math.max(0, messages.size() - lastNMessages), messages.size());
        StringBuilder sb = new StringBuilder();
        for (ConversationMessage m : recent) {
            sb.append(m.role().name()).append(": ").append(m.content()).append("\n");
        }
        return sb.toString();
    }

    public String getSessionId() { return sessionId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }

    // -----------------------------------------------------------------------

    /** Immutable record representing one turn in a conversation. */
    public record ConversationMessage(Role role, String content, Instant timestamp) {
        public enum Role { USER, ASSISTANT }
    }
}
