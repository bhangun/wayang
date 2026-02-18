package tech.kayys.wayang.eip.config;

import java.util.List;
import java.util.Map;

public record EnricherConfig(List<EnrichmentSource> sources, String mergeStrategy) {
    @SuppressWarnings("unchecked")
    public static EnricherConfig fromContext(Map<String, Object> context) {
        List<Map<String, Object>> sourcesData = (List<Map<String, Object>>) context.getOrDefault("sources", List.of());

        return new EnricherConfig(
                sourcesData.stream().map(EnrichmentSource::fromMap).toList(),
                (String) context.getOrDefault("mergeStrategy", "merge"));
    }
}
