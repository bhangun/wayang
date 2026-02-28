package tech.kayys.wayang.rag;

import java.time.Instant;

record ConversationTurn(
                String userMessage,
                String assistantMessage,
                Instant timestamp) {
}