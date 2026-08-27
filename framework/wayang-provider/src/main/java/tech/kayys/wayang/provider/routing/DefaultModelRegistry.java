package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.provider.ModelsDevRegistry;
import tech.kayys.wayang.resource.Modality;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory thread-safe model registry seeded from {@link DefaultModelCatalog},
 * with dynamic lookup and fallback synthesis.
 */
public class DefaultModelRegistry implements ModelRegistry {

    private final Map<String, ModelSpec> registry = new ConcurrentHashMap<>();

    public DefaultModelRegistry() {
        for (ModelSpec seed : DefaultModelCatalog.getSeedModels()) {
            registerModel(seed);
        }
    }

    @Override
    public List<ModelSpec> listModels() {
        return List.copyOf(registry.values());
    }

    @Override
    public Optional<ModelSpec> findModel(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return Optional.empty();
        }

        String normalized = modelId.strip().toLowerCase();
        ModelSpec direct = registry.get(normalized);
        if (direct != null) {
            return Optional.of(direct);
        }

        // Fuzzy lookup by prefix or substring
        for (Map.Entry<String, ModelSpec> entry : registry.entrySet()) {
            if (entry.getKey().contains(normalized) || normalized.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }

        // Synthesize dynamic ModelSpec if known provider/name pattern
        return Optional.of(new ModelSpec(
                modelId,
                inferProvider(modelId),
                128_000,
                4_096,
                true,
                false,
                Set.of(Modality.TEXT),
                1.00,
                3.00,
                800,
                0.80,
                false
        ));
    }

    @Override
    public void registerModel(ModelSpec spec) {
        if (spec != null && spec.modelId() != null) {
            registry.put(spec.modelId().toLowerCase(), spec);
        }
    }

    private String inferProvider(String modelId) {
        String lower = modelId.toLowerCase();
        if (lower.startsWith("gpt") || lower.startsWith("o1") || lower.startsWith("o3") || lower.startsWith("dall-e")) return "openai";
        if (lower.startsWith("claude")) return "anthropic";
        if (lower.startsWith("gemini")) return "google";
        if (lower.startsWith("deepseek")) return "deepseek";
        if (lower.startsWith("qwen")) return "openrouter";
        if (lower.startsWith("whisper")) return "groq";
        if (lower.startsWith("tripo")) return "tripo";
        if (lower.contains("local") || lower.startsWith("gollek") || lower.startsWith("flux") || lower.startsWith("trellis") || lower.startsWith("shap-e")) return "gollek";
        return "default";
    }
}
