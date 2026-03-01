package tech.kayys.wayang.guardrails.detector;

import java.util.List;
import java.util.stream.Collectors;

public record DetectionResults(List<DetectionResult> results) {

    public boolean hasBlockingIssues() {
        return results.stream()
                .anyMatch(r -> r.severity() == DetectionSeverity.BLOCK);
    }

    public boolean hasRedactableContent() {
        return results.stream()
                .anyMatch(r -> !r.findings().isEmpty());
    }

    public String summary() {
        return results.stream()
                .filter(r -> r.severity() == DetectionSeverity.BLOCK)
                .map(DetectionResult::message)
                .collect(Collectors.joining("; "));
    }

    public List<String> detectorIds() {
        return results.stream()
                .filter(r -> r.severity() == DetectionSeverity.BLOCK)
                .map(DetectionResult::detectorId)
                .toList();
    }

    public List<DetectionResult> getResults() {
        return results;
    }

    public boolean hasIssues() {
        return results.stream()
                .anyMatch(r -> r.severity() != DetectionSeverity.INFO);
    }
}
