package tech.kayys.gamelan.executor.camel.ai;

record ClaudeConfig(
        String apiKey,
        String model,
        String systemPrompt,
        double temperature,
        int maxTokens) {
}