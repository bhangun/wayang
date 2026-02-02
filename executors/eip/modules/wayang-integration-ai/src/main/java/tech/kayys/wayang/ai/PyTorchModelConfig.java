package tech.kayys.gamelan.executor.camel.ai;

record PyTorchModelConfig(
        String servingUrl,
        String modelName) {
}