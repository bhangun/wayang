package tech.kayys.wayang.models.core.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.models.api.domain.ModelCapability;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.domain.ModelType;
import tech.kayys.wayang.models.core.entity.ModelEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps between ModelMetadata and ModelEntity.
 */
@ApplicationScoped
public class ModelMapper {
    
    public ModelEntity toEntity(ModelMetadata metadata) {
        ModelEntity entity = new ModelEntity();
        entity.setModelId(metadata.getModelId());
        entity.setName(metadata.getName());
        entity.setVersion(metadata.getVersion());
        entity.setProvider(metadata.getProvider());
        entity.setType(metadata.getType().getValue());
        
        if (metadata.getCapabilities() != null) {
            entity.setCapabilities(metadata.getCapabilities().stream()
                .map(ModelCapability::getValue)
                .toArray(String[]::new));
        }
        
        entity.setMaxTokens(metadata.getMaxTokens());
        entity.setMaxOutputTokens(metadata.getMaxOutputTokens());
        
        if (metadata.getLatencyProfile() != null) {
            entity.setLatencyProfile(toLatencyMap(metadata.getLatencyProfile()));
        }
        
        if (metadata.getCostProfile() != null) {
            entity.setCostProfile(toCostMap(metadata.getCostProfile()));
        }
        
        if (metadata.getSupportedLanguages() != null) {
            entity.setSupportedLanguages(
                metadata.getSupportedLanguages().toArray(new String[0]));
        }
        
        entity.setDescription(metadata.getDescription());
        
        if (metadata.getTags() != null) {
            entity.setTags(metadata.getTags().toArray(new String[0]));
        }
        
        entity.setAttributes(metadata.getAttributes());
        entity.setEndpoint(metadata.getEndpoint());
        entity.setStatus(metadata.getStatus() != null ? 
            metadata.getStatus().name() : "ACTIVE");
        entity.setOwner(metadata.getOwner());
        
        return entity;
    }
    
    public ModelMetadata toMetadata(ModelEntity entity) {
        return ModelMetadata.builder()
            .modelId(entity.getModelId())
            .name(entity.getName())
            .version(entity.getVersion())
            .provider(entity.getProvider())
            .type(ModelType.fromValue(entity.getType()))
            .capabilities(entity.getCapabilities() != null ?
                Arrays.stream(entity.getCapabilities())
                    .map(ModelCapability::fromValue)
                    .collect(Collectors.toSet()) : Set.of())
            .maxTokens(entity.getMaxTokens())
            .maxOutputTokens(entity.getMaxOutputTokens())
            .latencyProfile(entity.getLatencyProfile() != null ?
                fromLatencyMap(entity.getLatencyProfile()) : null)
            .costProfile(entity.getCostProfile() != null ?
                fromCostMap(entity.getCostProfile()) : null)
            .supportedLanguages(entity.getSupportedLanguages() != null ?
                Arrays.asList(entity.getSupportedLanguages()) : null)
            .description(entity.getDescription())
            .tags(entity.getTags() != null ?
                Set.of(entity.getTags()) : null)
            .attributes(entity.getAttributes())
            .endpoint(entity.getEndpoint())
            .status(ModelMetadata.ModelStatus.valueOf(entity.getStatus()))
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .owner(entity.getOwner())
            .build();
    }
    
    public void updateEntity(ModelMetadata metadata, ModelEntity entity) {
        if (metadata.getName() != null) {
            entity.setName(metadata.getName());
        }
        if (metadata.getVersion() != null) {
            entity.setVersion(metadata.getVersion());
        }
        if (metadata.getCapabilities() != null) {
            entity.setCapabilities(metadata.getCapabilities().stream()
                .map(ModelCapability::getValue)
                .toArray(String[]::new));
        }
        if (metadata.getMaxTokens() != null) {
            entity.setMaxTokens(metadata.getMaxTokens());
        }
        if (metadata.getLatencyProfile() != null) {
            entity.setLatencyProfile(toLatencyMap(metadata.getLatencyProfile()));
        }
        if (metadata.getCostProfile() != null) {
            entity.setCostProfile(toCostMap(metadata.getCostProfile()));
        }
        if (metadata.getEndpoint() != null) {
            entity.setEndpoint(metadata.getEndpoint());
        }
        if (metadata.getStatus() != null) {
            entity.setStatus(metadata.getStatus().name());
        }
    }
    
    private Map<String, Object> toLatencyMap(ModelMetadata.LatencyProfile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("p50Ms", profile.getP50Ms());
        map.put("p95Ms", profile.getP95Ms());
        map.put("p99Ms", profile.getP99Ms());
        map.put("avgMs", profile.getAvgMs());
        return map;
    }
    
    private ModelMetadata.LatencyProfile fromLatencyMap(Map<String, Object> map) {
        return ModelMetadata.LatencyProfile.builder()
            .p50Ms((Integer) map.get("p50Ms"))
            .p95Ms((Integer) map.get("p95Ms"))
            .p99Ms((Integer) map.get("p99Ms"))
            .avgMs((Integer) map.get("avgMs"))
            .build();
    }
    
    private Map<String, Object> toCostMap(ModelMetadata.CostProfile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("perInputToken", profile.getPerInputToken());
        map.put("perOutputToken", profile.getPerOutputToken());
        map.put("perRequest", profile.getPerRequest());
        map.put("perEmbedding", profile.getPerEmbedding());
        return map;
    }
    
    private ModelMetadata.CostProfile fromCostMap(Map<String, Object> map) {
        return ModelMetadata.CostProfile.builder()
            .perInputToken(map.get("perInputToken") != null ?
                new java.math.BigDecimal(map.get("perInputToken").toString()) : null)
            .perOutputToken(map.get("perOutputToken") != null ?
                new java.math.BigDecimal(map.get("perOutputToken").toString()) : null)
            .perRequest(map.get("perRequest") != null ?
                new java.math.BigDecimal(map.get("perRequest").toString()) : null)
            .perEmbedding(map.get("perEmbedding") != null ?
                new java.math.BigDecimal(map.get("perEmbedding").toString()) : null)
            .build();
    }
}