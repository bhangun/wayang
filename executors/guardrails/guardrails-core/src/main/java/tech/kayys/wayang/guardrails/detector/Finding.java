package tech.kayys.wayang.guardrails.detector;

public record Finding(
                String type,
                String value,
                int startOffset,
                int endOffset,
                double confidence) {
}