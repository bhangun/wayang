package tech.kayys.wayang.service.impl;

import java.util.*;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.runtime.GenericRegistry;
import tech.kayys.wayang.spi.service.ResourceService;

/**
 * Default Resource Service
 */
public class DefaultResourceService implements ResourceService {
    
    private final GenericRegistry<Resource> registry = new GenericRegistry<>();
    
    @Override
    public <T extends Resource> void register(T resource) {
        registry.register(resource);
    }
    
    @Override
    public <T extends Resource> void unregister(ResourceId id) {
        registry.unregister(id);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> Optional<T> find(ResourceId id, Class<T> type) {
        return registry.find(id)
            .filter(type::isInstance)
            .map(type::cast);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> Optional<T> findByName(String name, Class<T> type) {
        return registry.findByName(name)
            .filter(type::isInstance)
            .map(type::cast);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> List<T> findAll(Class<T> type) {
        return registry.findAll().stream()
            .filter(type::isInstance)
            .map(type::cast)
            .toList();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> List<T> findByType(ResourceType type, Class<T> typeClass) {
        return registry.findByType(type).stream()
            .filter(typeClass::isInstance)
            .map(typeClass::cast)
            .toList();
    }
    
    @Override
    public boolean exists(ResourceId id) {
        return registry.exists(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return registry.existsByName(name);
    }
}