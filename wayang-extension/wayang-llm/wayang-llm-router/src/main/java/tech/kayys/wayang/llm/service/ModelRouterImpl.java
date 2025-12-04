package tech.kayys.wayang.models.router.service;

import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.domain.ModelCapability;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.exception.ModelException;
import tech.kayys.wayang.models.api.service.ModelRegistry;
import tech.kayys.wayang.models.api.service.ModelRouter;
import tech.kayys.wayang.models.router.strategy.RoutingStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ModelRouter with multi-strategy selection.
 * Uses capability matching, cost/latency optimization, and fallback logic.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ModelRouterImpl implements ModelRouter {
    
    private final ModelRegistry registry;
    private final Instance<RoutingStrategy> strategies;

    @Override
    public Uni<ModelMetadata> selectModel(ModelRequest request) {
        log.debug("Selecting model for request: {}", request.getRequestId());
        
        return getCandidateModels(request)
            .onItem().transform(candidates -> {
                if (candidates.isEmpty()) {
                    throw new ModelException("NO_MODEL_AVAILABLE",
                        "No models match the request criteria");
                }
                
                // Check preferred models first
                if (request.getModelHints() != null && 
                    request.getModelHints().getPreferred() != null) {
                    Optional<ModelMetadata> preferred = findPreferred(
                        candidates, request.getModelHints().getPreferred());
                    if (preferred.isPresent()) {
                        log.info("Using preferred model: {}", preferred.get().getModelId());
                        return preferred.get();
                    }
                }
                
                // Apply routing strategies
                List<RoutingStrategy.ScoredModel> scored = applyStrategies(candidates, request);
                
                if (scored.isEmpty()) {
                    throw new ModelException("NO_SUITABLE_MODEL",
                        "No models meet the routing criteria");
                }
                
                RoutingStrategy.ScoredModel selected = scored.get(0);
                log.info("Selected model: {} (score: {}, reasoning: {})",
                    selected.model().getModelId(), selected.score(), selected.reasoning());
                
                return selected.model();
            });
    }

    @Override
    public Uni<List<ModelMetadata>> getCandidates(ModelRequest request) {
        return getCandidateModels(request)
            .onItem().transform(candidates -> {
                List<RoutingStrategy.ScoredModel> scored = applyStrategies(candidates, request);
                return scored.stream()
                    .map(RoutingStrategy.ScoredModel::model)
                    .collect(Collectors.toList());
            });
    }

    @Override
    public Uni<ModelMetadata> selectFallback(ModelRequest request, String failedModelId) {
        log.info("Selecting fallback for failed model: {}", failedModelId);
        
        return getCandidateModels(request)
            .onItem().transform(candidates -> {
                // Remove failed model
                List<ModelMetadata> fallbackCandidates = candidates.stream()
                    .filter(m -> !m.getModelId().equals(failedModelId))
                    .collect(Collectors.toList());
                
                if (fallbackCandidates.isEmpty()) {
                    log.warn("No fallback models available");
                    return null;
                }
                
                List<RoutingStrategy.ScoredModel> scored = applyStrategies(
                    fallbackCandidates, request);
                
                if (scored.isEmpty()) {
                    return null;
                }
                
                ModelMetadata fallback = scored.get(0).model();
                log.info("Selected fallback model: {}", fallback.getModelId());
                return fallback;
            });
    }
    
    private Uni<List<ModelMetadata>> getCandidateModels(ModelRequest request) {
        // Determine required capabilities from request type
        Set<ModelCapability> required = determineRequiredCapabilities(request);
        
        if (request.getModelHints() != null && 
            request.getModelHints().getCapabilities() != null) {
            required.addAll(request.getModelHints().getCapabilities());
        }
        
        if (required.isEmpty()) {
            return registry.listModels();
        }
        
        return registry.findByCapabilities(required);
    }
    
    private Set<ModelCapability> determineRequiredCapabilities(ModelRequest request) {
        Set<ModelCapability> caps = new HashSet<>();
        
        switch (request.getType().toLowerCase()) {
            case "chat":
                caps.add(ModelCapability.CHAT);
                break;
            case "completion":
                caps.add(ModelCapability.COMPLETION);
                break;
            case "embed":
            case "embedding":
                caps.add(ModelCapability.EMBEDDING);
                break;
            case "multimodal":
                caps.add(ModelCapability.VISION);
                break;
        }
        
        if (Boolean.TRUE.equals(request.getStream())) {
            caps.add(ModelCapability.STREAMING);
        }
        
        if (request.getFunctions() != null && !request.getFunctions().isEmpty()) {
            caps.add(ModelCapability.FUNCTION_CALLING);
        }
        
        return caps;
    }
    
    private Optional<ModelMetadata> findPreferred(List<ModelMetadata> candidates,
                                                   List<String> preferred) {
        for (String modelId : preferred) {
            Optional<ModelMetadata> match = candidates.stream()
                .filter(m -> m.getModelId().equals(modelId))
                .findFirst();
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }
    
    private List<RoutingStrategy.ScoredModel> applyStrategies(
            List<ModelMetadata> candidates, 
            ModelRequest request) {
        
        Map<String, Double> aggregatedScores = new HashMap<>();
        Map<String, List<String>> reasonings = new HashMap<>();
        
        // Apply all strategies and aggregate scores
        for (RoutingStrategy strategy : strategies) {
            List<RoutingStrategy.ScoredModel> scored = strategy.rankModels(candidates, request);
            
            for (RoutingStrategy.ScoredModel sm : scored) {
                String modelId = sm.model().getModelId();
                aggregatedScores.merge(modelId, sm.score(), Double::sum);
                reasonings.computeIfAbsent(modelId, k -> new ArrayList<>())
                    .add(strategy.getClass().getSimpleName() + ": " + sm.reasoning());
            }
        }
        
        // Create final scored list
        return candidates.stream()
            .filter(m -> aggregatedScores.containsKey(m.getModelId()))
            .map(m -> new RoutingStrategy.ScoredModel(
                m,
                aggregatedScores.get(m.getModelId()),
                String.join("; ", reasonings.get(m.getModelId()))
            ))
            .filter(sm -> sm.score() > 0)
            .sorted((a, b) -> Double.compare(b.score(), a.score()))
            .collect(Collectors.toList());
    }
}