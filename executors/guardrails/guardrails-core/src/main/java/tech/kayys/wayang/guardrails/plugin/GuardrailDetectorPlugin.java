package tech.kayys.wayang.guardrails.plugin;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.guardrails.detector.CheckPhase;
import tech.kayys.wayang.guardrails.detector.DetectionResult;
import tech.kayys.wayang.plugin.WayangPlugin;

import java.util.Map;
import java.util.List;

/**
 * Interface for guardrail detector plugins.
 * These plugins are responsible for detecting potential issues in input or
 * output content.
 */
public interface GuardrailDetectorPlugin extends WayangPlugin {

    /**
     * Get the phases when this detector should be applied.
     */
    CheckPhase[] applicablePhases();

    /**
     * Detect issues in the provided text.
     * 
     * @param text The text to analyze
     * @return A DetectionResult indicating whether issues were found
     */
    default Uni<DetectionResult> detect(String text) {
        return detect(text, Map.of());
    }

    /**
     * Detect issues in the provided text with context metadata.
     * 
     * @param text     The text to analyze
     * @param metadata Contextual metadata (e.g. source text for hallucination
     *                 check)
     * @return A DetectionResult
     */
    Uni<DetectionResult> detect(String text, Map<String, Object> metadata);

    /**
     * Get the category of this detector (e.g., "pii", "toxicity", "bias").
     */
    String getCategory();

    /**
     * Get the severity level of this detector.
     */
    tech.kayys.wayang.guardrails.detector.DetectionSeverity getSeverity();
}