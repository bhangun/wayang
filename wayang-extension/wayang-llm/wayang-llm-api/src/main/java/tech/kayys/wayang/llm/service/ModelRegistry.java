package tech.kayys.wayang.models.api.service;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.models.api.domain.ModelCapability;
import tech.kayys.wayang.models.api.domain.ModelMetadata;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Model registry for capability discovery and metadata management.
 */
public interface ModelRegistry {
    
    /**
     * Register a new model.
     * 
     * @param metadata Model metadata
     * @return Registered metadata with generated fields
     */
    Uni<ModelMetadata> registerModel(ModelMetadata metadata);
    
    /**
     * Get model metadata by ID.
     * 
     * @param modelId Model identifier
     * @return Model metadata if exists
     */
    Uni<Optional<ModelMetadata>> getModel(String modelId);
    
    /**
     * List all registered models.
     * 
     * @return List of model metadata
     */
    Uni<List<ModelMetadata>> listModels();
    
    /**
     * Find models by capabilities.
     * 
     * @param capabilities Required capabilities
     * @return Matching models
     */
    Uni<List<ModelMetadata>> findByCapabilities(Set<ModelCapability> capabilities);
    
    /**
     * Find models by provider.
     * 
     * @param provider Provider identifier
     * @return Models from provider
     */
    Uni<List<ModelMetadata>> findByProvider(String provider);
    
    /**
     * Update model metadata.
     * 
     * @param modelId Model identifier
     * @param metadata Updated metadata
     * @return Updated metadata
     */
    Uni<ModelMetadata> updateModel(String modelId, ModelMetadata metadata);
    
    /**
     * Deactivate/remove model.
     * 
     * @param modelId Model identifier
     *@return Success indicator
     */
    Uni<Boolean> deactivateModel(String modelId);
}