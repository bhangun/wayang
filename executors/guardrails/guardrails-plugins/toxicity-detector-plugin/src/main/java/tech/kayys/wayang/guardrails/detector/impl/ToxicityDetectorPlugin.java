package tech.kayys.wayang.guardrails.detector.impl;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.guardrails.detector.DetectionResult;
import tech.kayys.wayang.guardrails.detector.DetectionSeverity;
import tech.kayys.wayang.guardrails.plugin.GuardrailDetectorPlugin;
import tech.kayys.wayang.guardrails.detector.CheckPhase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Toxicity Detector Plugin implementation.
 * Detects toxic or harmful content in text.
 */
@ApplicationScoped
public class ToxicityDetectorPlugin implements GuardrailDetectorPlugin {

    private static final Set<String> TOXIC_WORDS = Set.of(
        "hate", "kill", "murder", "attack", "violence", "abuse", "threat", "dangerous",
        "harmful", "destructive", "aggressive", "hostile", "offensive", "disgusting",
        "terrible", "awful", "horrible", "evil", "devil", "bastard", "bitch", "damn",
        "shit", "fuck", "asshole", "dick", "cunt", "slut", "whore", "rape", "rapist"
    );

    private static final Set<String> HATE_SPEECH_PATTERNS = Set.of(
        "racist", "discriminat", "bigot", "prejudice", "scapegoat", "oppress", "suppress",
        "inferior", "superior", "supremacist", "nazi", "fascist", "extremist"
    );

    @Override
    public String id() {
        return "toxicity-detector-plugin";
    }

    @Override
    public String name() {
        return "Toxicity Detector Plugin";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Detects toxic or harmful content in text";
    }

    @Override
    public CheckPhase[] applicablePhases() {
        return new CheckPhase[]{CheckPhase.PRE_EXECUTION, CheckPhase.POST_EXECUTION};
    }

    @Override
    public Uni<DetectionResult> detect(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Uni.createFrom().item(DetectionResult.safe(getCategory()));
        }

        String lowerText = text.toLowerCase();
        int toxicityScore = calculateToxicityScore(lowerText);

        if (toxicityScore >= 8) {
            return Uni.createFrom().item(
                DetectionResult.blocked(getCategory(), "High toxicity content detected")
            );
        } else if (toxicityScore >= 5) {
            return Uni.createFrom().item(
                DetectionResult.warning(getCategory(), "Moderate toxicity detected")
            );
        } else if (toxicityScore > 0) {
            return Uni.createFrom().item(
                DetectionResult.low(getCategory(), "Low toxicity detected")
            );
        }

        return Uni.createFrom().item(DetectionResult.safe(getCategory()));
    }

    private int calculateToxicityScore(String text) {
        int score = 0;

        // Check for toxic words
        for (String toxicWord : TOXIC_WORDS) {
            if (text.contains(toxicWord)) {
                score += 1;
            }
        }

        // Check for hate speech patterns
        for (String pattern : HATE_SPEECH_PATTERNS) {
            if (text.contains(pattern)) {
                score += 2; // Higher weight for hate speech
            }
        }

        // Check for repeated exclamation marks or question marks which might indicate aggression
        if (text.contains("!!!") || text.contains("???")) {
            score += 1;
        }

        return Math.min(score, 10); // Cap at 10
    }

    @Override
    public String getCategory() {
        return "toxicity";
    }

    @Override
    public DetectionSeverity getSeverity() {
        return DetectionSeverity.MEDIUM;
    }
}