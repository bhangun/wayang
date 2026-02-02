package tech.kayys.gamelan.executor.camel.ai;

record AzureOpenAIConfig(
        String resourceName,
        String deploymentName,
        String apiKey,
        double temperature,
        int maxTokens) {
}