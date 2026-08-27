package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.resource.Modality;
import java.util.Set;

/**
 * Model specifications, capabilities, input/output modalities, pricing, and performance characteristics.
 */
public record ModelSpec(
        String modelId,
        String family,
        String providerId,
        Set<ModelCapability> capabilities,
        Set<Modality> inputModalities,
        Set<Modality> outputModalities,
        long contextWindow,
        long maxOutputTokens,
        double costPerMillionInputTokens,
        double costPerMillionOutputTokens,
        long latencyP50Ms,
        double qualityScore,
        boolean isLocal
) {
    public ModelSpec {
        capabilities = capabilities != null ? Set.copyOf(capabilities) : Set.of(ModelCapability.TEXT_GENERATION);
        inputModalities = inputModalities != null ? Set.copyOf(inputModalities) : Set.of(Modality.TEXT);
        outputModalities = outputModalities != null ? Set.copyOf(outputModalities) : Set.of(Modality.TEXT);
    }

    /** Legacy / Convenience constructor for simple LLMs */
    public ModelSpec(
            String modelId,
            String providerId,
            long contextWindow,
            long maxOutputTokens,
            boolean supportsToolCalling,
            boolean supportsReasoning,
            Set<Modality> supportedModalities,
            double costPerMillionInputTokens,
            double costPerMillionOutputTokens,
            long latencyP50Ms,
            double qualityScore,
            boolean isLocal
    ) {
        this(
                modelId,
                inferFamily(modelId),
                providerId,
                buildCapabilities(supportsToolCalling, supportsReasoning, supportedModalities),
                supportedModalities != null ? supportedModalities : Set.of(Modality.TEXT),
                Set.of(Modality.TEXT),
                contextWindow,
                maxOutputTokens,
                costPerMillionInputTokens,
                costPerMillionOutputTokens,
                latencyP50Ms,
                qualityScore,
                isLocal
        );
    }

    public boolean supportsCapability(ModelCapability capability) {
        return capabilities.contains(capability);
    }

    public boolean supportsToolCalling() {
        return capabilities.contains(ModelCapability.TOOL_CALLING);
    }

    public boolean supportsReasoning() {
        return capabilities.contains(ModelCapability.REASONING);
    }

    public Set<Modality> supportedModalities() {
        return inputModalities;
    }

    public double estimateCost(long estimatedInputTokens, long estimatedOutputTokens) {
        return (estimatedInputTokens / 1_000_000.0) * costPerMillionInputTokens
                + (estimatedOutputTokens / 1_000_000.0) * costPerMillionOutputTokens;
    }

    private static String inferFamily(String modelId) {
        if (modelId == null) return "generic";
        String lower = modelId.toLowerCase();
        if (lower.startsWith("gpt") || lower.startsWith("o1") || lower.startsWith("o3")) return "openai";
        if (lower.startsWith("claude")) return "claude";
        if (lower.startsWith("gemini")) return "gemini";
        if (lower.startsWith("deepseek")) return "deepseek";
        if (lower.startsWith("qwen")) return "qwen";
        if (lower.startsWith("llama")) return "llama";
        if (lower.startsWith("whisper")) return "whisper";
        if (lower.startsWith("flux")) return "flux";
        if (lower.startsWith("shap-e") || lower.startsWith("point-e") || lower.startsWith("tripo")) return "3d-gen";
        return "generic";
    }

    private static Set<ModelCapability> buildCapabilities(boolean toolCalling, boolean reasoning, Set<Modality> modalities) {
        java.util.EnumSet<ModelCapability> caps = java.util.EnumSet.of(ModelCapability.CHAT, ModelCapability.TEXT_GENERATION);
        if (toolCalling) caps.add(ModelCapability.TOOL_CALLING);
        if (reasoning) caps.add(ModelCapability.REASONING);
        if (modalities != null) {
            if (modalities.contains(Modality.IMAGE)) caps.add(ModelCapability.VISION);
            if (modalities.contains(Modality.AUDIO)) caps.add(ModelCapability.AUDIO_UNDERSTANDING);
            if (modalities.contains(Modality.THREE_D)) caps.add(ModelCapability.THREE_D_UNDERSTANDING);
        }
        return caps;
    }
}
