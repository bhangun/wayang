package tech.kayys.wayang.models.router.strategy;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.domain.ModelCapability;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Routes based on capability matching.
 * Models with all required capabilities score higher.
 */
@ApplicationScoped
@Slf4j
public class CapabilityBasedStrategy implements RoutingStrategy {
    
    @Override
    public List<ScoredModel> rankModels(List<ModelMetadata> candidates, ModelRequest request) {
        Set<ModelCapability> required = request.getModelHints() != null ?
            request.getModelHints().getCapabilities() : Set.of();
        
        List<ScoredModel> scored = new ArrayList<>();
        
        for (ModelMetadata model : candidates) {
            double score = calculateCapabilityScore(model, required);
            String reasoning = String.format("Capability match: %.2f", score);
            scored.add(new ScoredModel(model, score, reasoning));
        }
        
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        return scored;
    }
    
    private double calculateCapabilityScore(ModelMetadata model, Set<ModelCapability> required) {
        if (required.isEmpty()) {
            return 1.0; // No specific requirements
        }
        
        Set<ModelCapability> modelCaps = model.getCapabilities();
        long matched = required.stream()
            .filter(modelCaps::contains)
            .count();
        
        // All required capabilities must be present
        if (matched < required.size()) {
            return 0.0;
        }
        
        // Bonus for additional capabilities
        double bonus = (modelCaps.size() - required.size()) * 0.1;
        return Math.min(1.0 + bonus, 1.5);
    }
}