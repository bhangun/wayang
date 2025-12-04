package tech.kayys.wayang.models.router.strategy;

import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;

import java.util.List;

/**
 * Strategy interface for model routing decisions.
 */
public interface RoutingStrategy {
    
    /**
     * Rank models by suitability for the request.
     * 
     * @param candidates Available models
     * @param request Model request
     * @return Models ranked by score (highest first)
     */
    List<ScoredModel> rankModels(List<ModelMetadata> candidates, ModelRequest request);
    
    /**
     * Model with routing score.
     */
    record ScoredModel(ModelMetadata model, double score, String reasoning) {}
}