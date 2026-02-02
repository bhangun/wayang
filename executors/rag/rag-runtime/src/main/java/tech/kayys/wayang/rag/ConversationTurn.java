package tech.kayys.gamelan.executor.rag.examples;

import java.time.Instant;

record ConversationTurn(
        String userMessage,
        String assistantMessage,
        Instant timestamp) {
}