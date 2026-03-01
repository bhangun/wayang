package tech.kayys.wayang.rag;

import java.time.Instant;

public record ConversationTurn(
        String userMessage,
        String assistantMessage,
        Instant timestamp) {
}