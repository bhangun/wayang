package tech.kayys.wayang.agent.gollek;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.gollek.multimodal.model.*;
import tech.kayys.gollek.multimodal.monitoring.MultimodalMonitoringService;
import tech.kayys.gollek.multimodal.service.MultimodalInferenceService;
import tech.kayys.wayang.agent.schema.AgentConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration service for Gollek multimodal inference.
 * Connects Wayang agent executors with Gollek multimodal capabilities.
 */
@ApplicationScoped
public class GollekIntegrationService {

    private static final Logger log = Logger.getLogger(GollekIntegrationService.class);

    @Inject
    MultimodalInferenceService gollekService;

    @Inject
    MultimodalMonitoringService monitoringService;

    /**
     * Execute text inference through Gollek.
     *
     * @param model  Model to use
     * @param prompt Input prompt
     * @param config Agent configuration
     * @return Inference response
     */
    public Uni<String> executeTextInference(String model, String prompt, AgentConfig config) {
        log.infof("Executing Gollek text inference: model=%s, prompt_length=%d",
                model, prompt != null ? prompt.length() : 0);

        try {
            // Record request
            monitoringService.recordRequest();

            // Build multimodal request
            MultimodalRequest request = buildMultimodalRequest(model, prompt, config);

            // Execute inference
            return gollekService.infer(request)
                    .onItem().transform(response -> {
                        // Record success
                        monitoringService.recordSuccess(response.getDurationMs(),
                                response.getUsage() != null ? response.getUsage().getOutputTokens() : 0);

                        // Extract text from response
                        if (response.getOutputs() != null && response.getOutputs().length > 0) {
                            return response.getOutputs()[0].getText();
                        }
                        return "";
                    })
                    .onFailure().recoverWithItem(error -> {
                        // Record failure
                        monitoringService.recordFailure("inference_error");
                        log.errorf("Gollek inference failed: %s", error.getMessage());
                        return "Error: " + error.getMessage();
                    });

        } catch (Exception e) {
            log.errorf("Failed to execute Gollek inference: %s", e.getMessage());
            monitoringService.recordFailure("execution_error");
            return Uni.createFrom().item("Error: " + e.getMessage());
        }
    }

    /**
     * Execute streaming text inference through Gollek.
     *
     * @param model  Model to use
     * @param prompt Input prompt
     * @param config Agent configuration
     * @return Stream of response chunks
     */
    public Multi<String> executeStreamingInference(String model, String prompt, AgentConfig config) {
        log.infof("Executing Gollek streaming inference: model=%s", model);

        try {
            // Record request
            monitoringService.recordRequest();
            monitoringService.recordStreamStart();

            // Build multimodal request
            MultimodalRequest request = buildMultimodalRequest(model, prompt, config);

            // Execute streaming inference
            return gollekService.inferStream(request)
                    .onItem().transform(response -> {
                        if (response.getOutputs() != null && response.getOutputs().length > 0) {
                            String text = response.getOutputs()[0].getText();
                            if (text != null && !text.isEmpty()) {
                                return text;
                            }
                        }
                        return "";
                    })
                    .filter(s -> !s.isEmpty())
                    .onCompletion().invoke(() -> {
                        monitoringService.recordStreamEnd();
                        log.info("Streaming inference completed");
                    })
                    .onFailure().invoke(error -> {
                        monitoringService.recordStreamEnd();
                        monitoringService.recordFailure("stream_error");
                        log.errorf("Streaming inference failed: %s", error.getMessage());
                    });

        } catch (Exception e) {
            log.errorf("Failed to execute Gollek streaming inference: %s", e.getMessage());
            monitoringService.recordFailure("stream_execution_error");
            return Multi.createFrom().failure(e);
        }
    }

    /**
     * Execute multimodal inference (text + image) through Gollek.
     *
     * @param model     Model to use
     * @param prompt    Input prompt
     * @param imageData Image data (base64 or URL)
     * @param config    Agent configuration
     * @return Inference response
     */
    public Uni<String> executeMultimodalInference(String model, String prompt,
            String imageData, AgentConfig config) {
        log.infof("Executing Gollek multimodal inference: model=%s", model);

        try {
            // Record request
            monitoringService.recordRequest();

            // Build multimodal request with image
            MultimodalRequest request = buildMultimodalRequest(model, prompt, config);

            // Add image if provided
            if (imageData != null && !imageData.isEmpty()) {
                MultimodalContent[] inputs = request.getInputs();
                MultimodalContent[] newInputs = new MultimodalContent[inputs.length + 1];
                System.arraycopy(inputs, 0, newInputs, 0, inputs.length);

                if (imageData.startsWith("http")) {
                    newInputs[inputs.length] = MultimodalContent.ofImageUri(imageData, "image/jpeg");
                } else {
                    newInputs[inputs.length] = MultimodalContent.ofBase64Image(
                            java.util.Base64.getDecoder().decode(imageData),
                            "image/jpeg");
                }

                request.setInputs(newInputs);
            }

            // Execute inference
            return gollekService.infer(request)
                    .onItem().transform(response -> {
                        monitoringService.recordSuccess(response.getDurationMs(),
                                response.getUsage() != null ? response.getUsage().getOutputTokens() : 0);

                        if (response.getOutputs() != null && response.getOutputs().length > 0) {
                            return response.getOutputs()[0].getText();
                        }
                        return "";
                    })
                    .onFailure().recoverWithItem(error -> {
                        monitoringService.recordFailure("multimodal_error");
                        log.errorf("Gollek multimodal inference failed: %s", error.getMessage());
                        return "Error: " + error.getMessage();
                    });

        } catch (Exception e) {
            log.errorf("Failed to execute Gollek multimodal inference: %s", e.getMessage());
            monitoringService.recordFailure("multimodal_execution_error");
            return Uni.createFrom().item("Error: " + e.getMessage());
        }
    }

    /**
     * Get available models from Gollek.
     *
     * @return List of available model names
     */
    public List<String> getAvailableModels() {
        try {
            return gollekService.getAvailableProcessors();
        } catch (Exception e) {
            log.errorf("Failed to get available models: %s", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get current health status.
     *
     * @return Health status
     */
    public String getHealthStatus() {
        try {
            MultimodalMonitoringService.HealthStatus health = monitoringService.getHealthStatus();
            return health.overall;
        } catch (Exception e) {
            log.errorf("Failed to get health status: %s", e.getMessage());
            return "DOWN";
        }
    }

    /**
     * Get current metrics.
     *
     * @return Metrics map
     */
    public Map<String, Object> getMetrics() {
        try {
            MultimodalMonitoringService.MonitoringMetrics metrics = monitoringService.getMetrics();
            Map<String, Object> metricsMap = new HashMap<>();
            metricsMap.put("totalRequests", metrics.totalRequests);
            metricsMap.put("successfulRequests", metrics.successfulRequests);
            metricsMap.put("failedRequests", metrics.failedRequests);
            metricsMap.put("errorRate", metrics.errorRate);
            metricsMap.put("p95LatencyMs", metrics.p95LatencyMs);
            metricsMap.put("p99LatencyMs", metrics.p99LatencyMs);
            metricsMap.put("activeRequests", metrics.activeRequests);
            metricsMap.put("requestsPerSecond", metrics.requestsPerSecond);
            return metricsMap;
        } catch (Exception e) {
            log.errorf("Failed to get metrics: %s", e.getMessage());
            return Map.of();
        }
    }

    // Private helper methods

    private MultimodalRequest buildMultimodalRequest(String model, String prompt, AgentConfig config) {
        MultimodalRequest.Builder builder = MultimodalRequest.builder()
                .model(model != null ? model : "default")
                .inputs(MultimodalContent.ofText(prompt != null ? prompt : ""));

        // Apply configuration
        if (config != null) {
            MultimodalRequest.OutputConfig.Builder configBuilder = MultimodalRequest.OutputConfig.builder();

            if (config.getMaxTokens() != null) {
                configBuilder.maxTokens(config.getMaxTokens());
            }
            if (config.getTemperature() != null) {
                configBuilder.temperature(config.getTemperature());
            }
            if (config.getTopP() != null) {
                configBuilder.topP(config.getTopP());
            }

            builder.outputConfig(configBuilder.build());
        }

        return builder.build();
    }
}
