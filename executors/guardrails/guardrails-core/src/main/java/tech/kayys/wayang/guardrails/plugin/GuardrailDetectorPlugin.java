package tech.kayys.wayang.guardrails.plugin;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.guardrails.detector.DetectionResult;
import tech.kayys.wayang.plugin.WayangPlugin;

/**
 * Interface for guardrail detector plugins.
 * These plugins are responsible for detecting potential issues in input or output content.
 */
public interface GuardrailDetectorPlugin extends WayangPlugin {
    
    /**
     * The phase when this detector should be applied.
     */
    enum CheckPhase {
        PRE_EXECUTION,  // Check before node execution
        POST_EXECUTION  // Check after node execution
    }
    
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
    Uni<DetectionResult> detect(String text);
    
    /**
     * Get the category of this detector (e.g., "pii", "toxicity", "bias").
     */
    String getCategory();
    
    /**
     * Get the severity level of this detector.
     */
    tech.kayys.wayang.guardrails.detector.DetectionSeverity getSeverity();
}