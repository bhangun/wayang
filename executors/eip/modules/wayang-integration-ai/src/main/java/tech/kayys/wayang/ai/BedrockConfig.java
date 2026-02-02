package tech.kayys.gamelan.executor.camel.ai;

record BedrockConfig(
        String modelId,
        String region,
        double temperature,
        int maxTokens) {
}