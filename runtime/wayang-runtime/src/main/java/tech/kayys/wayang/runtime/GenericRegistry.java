package tech.kayys.wayang.runtime;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.registry.Registry;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Generic registry implementation
 */
public class GenericRegistry<T extends Resource> implements Registry<T> {
    
    private final Map<ResourceId, T> resources = new ConcurrentHashMap<>();
    private final Map<String, T> nameIndex = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Set<ResourceId>>> labelIndex = new ConcurrentHashMap<>();
    
    @Override
    public void register(T resource) {
        ResourceId id = resource.id();
        resources.put(id, resource);
        
        String name = resource.metadata().name();
        if (name != null) {
            nameIndex.put(name.toLowerCase(), resource);
        }
        
        // Index labels
        for (Map.Entry<String, String> label : resource.metadata().labels().entrySet()) {
            labelIndex.computeIfAbsent(label.getKey(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(label.getValue(), v -> ConcurrentHashMap.newKeySet())
                .add(id);
        }
    }
    
    @Override
    public void unregister(ResourceId id) {
        T resource = resources.remove(id);
        if (resource != null) {
            String name = resource.metadata().name();
            if (name != null) {
                nameIndex.remove(name.toLowerCase(), resource);
            }
            
            // Remove from label index
            for (Map.Entry<String, String> label : resource.metadata().labels().entrySet()) {
                Map<String, Set<ResourceId>> labelMap = labelIndex.get(label.getKey());
                if (labelMap != null) {
                    Set<ResourceId> ids = labelMap.get(label.getValue());
                    if (ids != null) {
                        ids.remove(id);
                        if (ids.isEmpty()) {
                            labelMap.remove(label.getValue());
                        }
                    }
                    if (labelMap.isEmpty()) {
                        labelIndex.remove(label.getKey());
                    }
                }
            }
        }
    }
    
    @Override
    public Optional<T> find(ResourceId id) {
        return Optional.ofNullable(resources.get(id));
    }
    
    @Override
    public Optional<T> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(nameIndex.get(name.toLowerCase()));
    }
    
    @Override
    public List<T> findAll() {
        return List.copyOf(resources.values());
    }
    
    @Override
    public List<T> findByType(ResourceType type) {
        return resources.values().stream()
            .filter(r -> r.type().equals(type))
            .toList();
    }
    
    @Override
    public List<T> findByLabel(String key, String value) {
        Map<String, Set<ResourceId>> labelMap = labelIndex.get(key);
        if (labelMap == null) return List.of();
        
        Set<ResourceId> ids = labelMap.get(value);
        if (ids == null || ids.isEmpty()) return List.of();
        
        return ids.stream()
            .map(resources::get)
            .filter(Objects::nonNull)
            .toList();
    }
    
    @Override
    public boolean exists(ResourceId id) {
        return resources.containsKey(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        if (name == null) return false;
        return nameIndex.containsKey(name.toLowerCase());
    }
    
    @Override
    public int count() {
        return resources.size();
    }
    
    @Override
    public void clear() {
        resources.clear();
        nameIndex.clear();
        labelIndex.clear();
    }
}
