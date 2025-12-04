package tech.kayys.wayang.models.core.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.domain.ModelCapability;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.exception.ModelException;
import tech.kayys.wayang.models.api.exception.ModelNotFoundException;
import tech.kayys.wayang.models.api.service.ModelRegistry;
import tech.kayys.wayang.models.core.entity.ModelEntity;
import tech.kayys.wayang.models.core.mapper.ModelMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of Model Registry using PostgreSQL.
 * Provides CRUD operations for model metadata with reactive access.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ModelRegistryImpl implements ModelRegistry {
    
    private final ModelMapper mapper;

    @Override
    @WithTransaction
    public Uni<ModelMetadata> registerModel(ModelMetadata metadata) {
        log.info("Registering model: {}", metadata.getModelId());
        
        return ModelEntity.findById(metadata.getModelId())
            .onItem().transformToUni(existing -> {
                if (existing != null) {
                    return Uni.createFrom().failure(
                        new ModelException("MODEL_ALREADY_EXISTS", 
                            "Model already exists: " + metadata.getModelId()));
                }
                
                ModelEntity entity = mapper.toEntity(metadata);
                entity.setCreatedAt(Instant.now());
                entity.setUpdatedAt(Instant.now());
                
                return entity.persist()
                    .onItem().transform(mapper::toMetadata)
                    .invoke(m -> log.info("Model registered successfully: {}", m.getModelId()));
            });
    }

    @Override
    public Uni<Optional<ModelMetadata>> getModel(String modelId) {
        return ModelEntity.<ModelEntity>findById(modelId)
            .onItem().transform(entity -> 
                Optional.ofNullable(entity).map(mapper::toMetadata));
    }

    @Override
    public Uni<List<ModelMetadata>> listModels() {
        return ModelEntity.findActive()
            .onItem().transform(entities -> entities.stream()
                .map(mapper::toMetadata)
                .collect(Collectors.toList()));
    }

    @Override
    public Uni<List<ModelMetadata>> findByCapabilities(Set<ModelCapability> capabilities) {
        Set<String> capStrings = capabilities.stream()
            .map(ModelCapability::getValue)
            .collect(Collectors.toSet());
            
        return ModelEntity.findByCapabilities(capStrings)
            .onItem().transform(entities -> entities.stream()
                .map(mapper::toMetadata)
                .collect(Collectors.toList()));
    }

    @Override
    public Uni<List<ModelMetadata>> findByProvider(String provider) {
        return ModelEntity.findByProvider(provider)
            .onItem().transform(entities -> entities.stream()
                .map(mapper::toMetadata)
                .collect(Collectors.toList()));
    }

    @Override
    @WithTransaction
    public Uni<ModelMetadata> updateModel(String modelId, ModelMetadata metadata) {
        log.info("Updating model: {}", modelId);
        
        return ModelEntity.<ModelEntity>findById(modelId)
            .onItem().transformToUni(entity -> {
                if (entity == null) {
                    return Uni.createFrom().failure(
                        new ModelNotFoundException(modelId));
                }
                
                mapper.updateEntity(metadata, entity);
                entity.setUpdatedAt(Instant.now());
                
                return entity.persist()
                    .onItem().transform(mapper::toMetadata)
                    .invoke(m -> log.info("Model updated successfully: {}", modelId));
            });
    }

    @Override
    @WithTransaction
    public Uni<Boolean> deactivateModel(String modelId) {
        log.info("Deactivating model: {}", modelId);
        
        return ModelEntity.<ModelEntity>findById(modelId)
            .onItem().transformToUni(entity -> {
                if (entity == null) {
                    return Uni.createFrom().failure(
                        new ModelNotFoundException(modelId));
                }
                
                entity.setStatus("DISABLED");
                entity.setUpdatedAt(Instant.now());
                
                return entity.persist()
                    .onItem().transform(e -> true)
                    .invoke(() -> log.info("Model deactivated: {}", modelId));
            });
    }
}