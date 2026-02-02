package tech.kayys.gamelan.executor.camel.ai;

import java.time.Instant;

record LLMResponse(
        String content,
        String model,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        Instant generatedAt) {
}