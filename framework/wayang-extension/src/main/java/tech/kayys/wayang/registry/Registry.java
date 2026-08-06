package tech.kayys.wayang.registry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.*;

import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Registry for finding and managing resources.
 */
public interface Registry<T extends Resource> {
    
    void register(T resource);
    
    void unregister(ResourceId id);
    
    Optional<T> find(ResourceId id);
    
    Optional<T> findByName(String name);
    
    List<T> findAll();
    
    List<T> findByType(ResourceType type);
    
    List<T> findByLabel(String key, String value);
    
    boolean exists(ResourceId id);
    
    boolean existsByName(String name);
    
    int count();
    
    void clear();
}