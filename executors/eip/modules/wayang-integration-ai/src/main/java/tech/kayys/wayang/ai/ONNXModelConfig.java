package tech.kayys.gamelan.executor.camel.ai;

import java.util.Map;

record ONNXModelConfig(
        String modelPath,
        Map<String, Object> inputSpec) {
}