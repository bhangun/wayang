package tech.kayys.wayang.guardrails.detector;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.guardrails.plugin.GuardrailDetectorPlugin;

import java.util.*;

@ApplicationScoped
public class ToxicityDetector implements GuardrailDetectorPlugin {

    @Override
    public String id() {
        return "toxicity-detector";
    }

    private static final Set<String> TOXIC_WORDS = Set.of(
            "hate", "stupid", "idiot", "kill", "worthless", "ugly",
            "disgusting", "pathetic", "moron", "scum", "trash");

    @Override
    public String name() {
        return "Toxicity Detector";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Detects toxic content in text";
    }

    private static final Set<String> HATE_INDICATORS = Set.of(
            "race", "gender", "religion", "ethnicity", "sexual orientation");

    @Override
    public CheckPhase[] applicablePhases() {
        return new CheckPhase[] { CheckPhase.POST_EXECUTION };
    }

    @Override
    public String getCategory() {
        return "toxicity";
    }

    @Override
    public DetectionSeverity getSeverity() {
        return DetectionSeverity.BLOCK;
    }

    @Override
    public Uni<DetectionResult> detect(String text, Map<String, Object> metadata) {
        return Uni.createFrom().item(() -> {
            String lowerText = text.toLowerCase();

            List<String> toxicWordsFound = TOXIC_WORDS.stream()
                    .filter(lowerText::contains)
                    .toList();

            boolean hasHateSpeech = HATE_INDICATORS.stream()
                    .anyMatch(indicator -> lowerText.contains(indicator) &&
                            toxicWordsFound.stream().anyMatch(lowerText::contains));

            double toxicityScore = calculateToxicityScore(lowerText, toxicWordsFound);

            if (toxicityScore > 0.8 || hasHateSpeech) {
                return DetectionResult.blocked(getCategory(),
                        "High toxicity detected" + (hasHateSpeech ? " (Potential Hate Speech)" : ""));
            } else if (toxicityScore > 0.5) {
                List<Finding> findings = toxicWordsFound.stream()
                        .map(word -> new Finding("TOXIC_WORD", word, lowerText.indexOf(word),
                                lowerText.indexOf(word) + word.length(), 1.0))
                        .toList();
                return DetectionResult.warning(getCategory(), "Moderate toxicity detected", findings);
            }

            return DetectionResult.safe(getCategory());
        });
    }

    private double calculateToxicityScore(String text, List<String> toxicWordsFound) {
        if (toxicWordsFound.isEmpty())
            return 0.0;

        double wordScore = toxicWordsFound.size() * 0.2;

        // Check for intensity modifiers
        double intensityModifier = 1.0;
        if (text.contains("very ") || text.contains("extremely ")) {
            intensityModifier = 1.5;
        }

        // Check for all caps (shouting)
        long capsWords = Arrays.stream(text.split("\\s+"))
                .filter(word -> word.length() > 2 && word.equals(word.toUpperCase()))
                .count();
        double capsModifier = 1.0 + (capsWords * 0.1);

        return Math.min(wordScore * intensityModifier * capsModifier, 1.0);
    }
}