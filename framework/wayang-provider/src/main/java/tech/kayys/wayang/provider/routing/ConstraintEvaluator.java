package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.resource.Modality;
import java.util.Optional;

/**
 * Evaluates Hard Constraints for candidate models.
 * A failure of any hard constraint immediately rejects the candidate with an explainable diagnostic reason.
 */
public class ConstraintEvaluator {

    public record EvaluationResult(boolean isAllowed, String rejectionReason) {
        public static EvaluationResult ok() { return new EvaluationResult(true, null); }
        public static EvaluationResult reject(String reason) { return new EvaluationResult(false, reason); }
    }

    public EvaluationResult evaluate(
            ModelSpec model,
            InferenceRequirements requirements,
            InferencePolicy policy
    ) {
        if (model == null) {
            return EvaluationResult.reject("Null model spec");
        }

        // 1. Hard constraint: Input Modalities
        if (!model.inputModalities().containsAll(requirements.inputModalities())) {
            return EvaluationResult.reject(String.format("Missing required input modalities: required %s, model supports %s",
                    requirements.inputModalities(), model.inputModalities()));
        }

        // 2. Hard constraint: Output Modalities
        if (!model.outputModalities().containsAll(requirements.outputModalities())) {
            return EvaluationResult.reject(String.format("Missing required output modalities: required %s, model produces %s",
                    requirements.outputModalities(), model.outputModalities()));
        }

        // 3. Hard constraint: Required Capabilities (OCR, 3D, Vision, STT, TTS, Tool Calling, Reasoning, etc.)
        for (ModelCapability requiredCap : requirements.requiredCapabilities()) {
            if (!model.supportsCapability(requiredCap)) {
                return EvaluationResult.reject("Does not support required capability: " + requiredCap);
            }
        }

        // 4. Hard constraint: Context Window
        if (requirements.requiredContextTokens() > model.contextWindow()) {
            return EvaluationResult.reject(String.format("Context limit (%d) < required tokens (%d)",
                    model.contextWindow(), requirements.requiredContextTokens()));
        }

        // 5. Policy Hard Filters: Denied Providers & Models
        if (policy.deniedProviders().contains(model.providerId())) {
            return EvaluationResult.reject("Provider '" + model.providerId() + "' denied by policy");
        }
        if (policy.deniedModels().contains(model.modelId())) {
            return EvaluationResult.reject("Model '" + model.modelId() + "' denied by policy");
        }

        // 6. Policy Hard Filters: Allowed Providers & Models
        if (!policy.allowedProviders().isEmpty() && !policy.allowedProviders().contains(model.providerId())) {
            return EvaluationResult.reject("Provider '" + model.providerId() + "' not in allowed providers list");
        }
        if (!policy.allowedModels().isEmpty() && !policy.allowedModels().contains(model.modelId())) {
            return EvaluationResult.reject("Model not in allowed models list");
        }

        // 7. Hard Budget Limit per turn
        long inputTokens = Math.max(requirements.requiredContextTokens(), 2048);
        double estimatedCost = model.estimateCost(inputTokens, 1024);
        if (policy.maxCostPerTurn() != null && estimatedCost > policy.maxCostPerTurn()) {
            return EvaluationResult.reject(String.format("Estimated cost ($%.4f) exceeds turn budget limit ($%.4f)",
                    estimatedCost, policy.maxCostPerTurn()));
        }
        if (requirements.maxCostBudget() != null && estimatedCost > requirements.maxCostBudget()) {
            return EvaluationResult.reject(String.format("Estimated cost ($%.4f) exceeds request cost budget ($%.4f)",
                    estimatedCost, requirements.maxCostBudget()));
        }

        return EvaluationResult.ok();
    }
}
