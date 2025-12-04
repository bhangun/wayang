package tech.kayys.wayang.models.router.strategy;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Routes based on cost and latency constraints.
 * Balances performance and cost efficiency.
 */
@ApplicationScoped
@Slf4j
public class CostLatencyStrategy implements RoutingStrategy {
    
    private static final double LATENCY_WEIGHT = 0.6;
    private static final double COST_WEIGHT = 0.4;
    
    @Override
    public List<ScoredModel> rankModels(List<ModelMetadata> candidates, ModelRequest request) {
        Integer maxLatency = request.getModelHints() != null ?
            request.getModelHints().getMaxLatencyMs() : null;
        Double maxCost = request.getModelHints() != null ?
            request.getModelHints().getMaxCostUsd() : null;
        
        List<ScoredModel> scored = new ArrayList<>();
        
        // Find min/max for normalization
        double minLatency = candidates.stream()
            .filter(m -> m.getLatencyProfile() != null)
            .mapToInt(m -> m.getLatencyProfile().getP95Ms())
            .min().orElse(100);
        
        double maxLatencyValue = candidates.stream()
            .filter(m -> m.getLatencyProfile() != null)
            .mapToInt(m -> m.getLatencyProfile().getP95Ms())
            .max().orElse(1000);
        
        for (ModelMetadata model : candidates) {
            double score = calculateScore(model, maxLatency, maxCost, minLatency, maxLatencyValue);
            String reasoning = buildReasoning(model, score);
            scored.add(new ScoredModel(model, score, reasoning));
        }
        
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        return scored;
    }
    
    private double calculateScore(ModelMetadata model, Integer maxLatency, 
                                  Double maxCost, double minLatency, double maxLatencyValue) {
        double latencyScore = calculateLatencyScore(model, maxLatency, minLatency, maxLatencyValue);
        double costScore = calculateCostScore(model, maxCost);
        
        // If constraints violated, zero score
        if (latencyScore == 0 || costScore == 0) {
            return 0.0;
        }
        
        return (latencyScore * LATENCY_WEIGHT) + (costScore * COST_WEIGHT);
    }
    
    private double calculateLatencyScore(ModelMetadata model, Integer maxLatency,
                                        double minLatency, double maxLatencyValue) {
        if (model.getLatencyProfile() == null) {
            return 0.5; // Unknown latency, neutral score
        }
        
        int p95 = model.getLatencyProfile().getP95Ms();
        
        // Hard constraint
        if (maxLatency != null && p95 > maxLatency) {
            return 0.0;
        }
        
        // Normalize: lower latency = higher score
        double normalized = 1.0 - ((p95 - minLatency) / (maxLatencyValue - minLatency));
        return Math.max(0.0, Math.min(1.0, normalized));
    }
    
    private double calculateCostScore(ModelMetadata model, Double maxCost) {
        if (model.getCostProfile() == null) {
            return 0.5; // Unknown cost, neutral score
        }
        
        // Estimate cost for typical request (1000 tokens)
        BigDecimal inputCost = model.getCostProfile().getPerInputToken();
        BigDecimal outputCost = model.getCostProfile().getPerOutputToken();
        
        if (inputCost == null || outputCost == null) {
            return 0.5;
        }
        
        double estimatedCost = inputCost.multiply(BigDecimal.valueOf(500))
            .add(outputCost.multiply(BigDecimal.valueOf(500)))
            .doubleValue();
        
        // Hard constraint
        if (maxCost != null && estimatedCost > maxCost) {
            return 0.0;
        }
        
        // Lower cost = higher score (inverse exponential)
        return Math.exp(-estimatedCost * 10);
    }
    
    private String buildReasoning(ModelMetadata model, double score) {
        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(String.format("%.3f", score));
        
        if (model.getLatencyProfile() != null) {
            sb.append(", P95: ").append(model.getLatencyProfile().getP95Ms()).append("ms");
        }
        
        if (model.getCostProfile() != null && model.getCostProfile().getPerInputToken() != null) {
            sb.append(", Cost: $").append(model.getCostProfile().getPerInputToken());
        }
        
        return sb.toString();
    }
}