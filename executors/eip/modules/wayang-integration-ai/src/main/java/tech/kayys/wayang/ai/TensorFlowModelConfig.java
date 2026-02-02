package tech.kayys.gamelan.executor.camel.ai;

record TensorFlowModelConfig(
        String servingUrl,
        String modelName,
        String signatureName) {
}