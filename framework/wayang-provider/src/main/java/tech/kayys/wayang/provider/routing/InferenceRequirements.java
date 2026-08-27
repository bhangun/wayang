package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.resource.Modality;
import java.util.List;
import java.util.Set;

/**
 * Requirements derived from context planning, task nature, and agent definition.
 * Distinguishes Hard Constraints (must be met or candidate rejected) from Soft Preferences (influence scoring).
 */
public record InferenceRequirements(
        Set<Modality> inputModalities,
        Set<Modality> outputModalities,
        Set<ModelCapability> requiredCapabilities,
        long requiredContextTokens,
        String latencyPreference,
        Double maxCostBudget,
        List<String> requiredFeatures
) {
    public InferenceRequirements {
        inputModalities = inputModalities != null ? Set.copyOf(inputModalities) : Set.of(Modality.TEXT);
        outputModalities = outputModalities != null ? Set.copyOf(outputModalities) : Set.of(Modality.TEXT);
        requiredCapabilities = requiredCapabilities != null ? Set.copyOf(requiredCapabilities) : Set.of(ModelCapability.TEXT_GENERATION);
        requiredFeatures = requiredFeatures != null ? List.copyOf(requiredFeatures) : List.of();
    }

    public static InferenceRequirements defaults() {
        return new InferenceRequirements(
                Set.of(Modality.TEXT),
                Set.of(Modality.TEXT),
                Set.of(ModelCapability.TEXT_GENERATION),
                8192,
                "BALANCED",
                null,
                List.of()
        );
    }

    public static InferenceRequirements of(Set<Modality> modalities, boolean toolCalling, long contextTokens) {
        java.util.EnumSet<ModelCapability> caps = java.util.EnumSet.of(ModelCapability.TEXT_GENERATION);
        if (toolCalling) caps.add(ModelCapability.TOOL_CALLING);
        if (modalities != null && modalities.contains(Modality.IMAGE)) caps.add(ModelCapability.VISION);
        return new InferenceRequirements(
                modalities != null ? modalities : Set.of(Modality.TEXT),
                Set.of(Modality.TEXT),
                caps,
                contextTokens,
                "BALANCED",
                null,
                List.of()
        );
    }

    public static InferenceRequirements of(Set<Modality> modalities, boolean toolCalling, boolean reasoning, long contextTokens) {
        java.util.EnumSet<ModelCapability> caps = java.util.EnumSet.of(ModelCapability.TEXT_GENERATION);
        if (toolCalling) caps.add(ModelCapability.TOOL_CALLING);
        if (reasoning) caps.add(ModelCapability.REASONING);
        if (modalities != null && modalities.contains(Modality.IMAGE)) caps.add(ModelCapability.VISION);
        return new InferenceRequirements(
                modalities != null ? modalities : Set.of(Modality.TEXT),
                Set.of(Modality.TEXT),
                caps,
                contextTokens,
                "BALANCED",
                null,
                List.of()
        );
    }

    public static InferenceRequirements multimodal(
            Set<Modality> inputs,
            Set<Modality> outputs,
            Set<ModelCapability> capabilities
    ) {
        return new InferenceRequirements(
                inputs,
                outputs,
                capabilities,
                1024,
                "BALANCED",
                null,
                List.of()
        );
    }

    public Set<Modality> modalities() {
        return inputModalities;
    }

    public boolean requiresToolCalling() {
        return requiredCapabilities.contains(ModelCapability.TOOL_CALLING);
    }

    public boolean requiresReasoning() {
        return requiredCapabilities.contains(ModelCapability.REASONING);
    }

    public boolean requiresVision() {
        return requiredCapabilities.contains(ModelCapability.VISION) || inputModalities.contains(Modality.IMAGE);
    }

    public boolean requires3D() {
        return requiredCapabilities.contains(ModelCapability.THREE_D_GENERATION)
                || requiredCapabilities.contains(ModelCapability.THREE_D_UNDERSTANDING)
                || outputModalities.contains(Modality.THREE_D)
                || inputModalities.contains(Modality.THREE_D);
    }
}
