package tech.kayys.wayang.service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Optional;

import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Resource Service - manages all resources
 */
public interface ResourceService {
    <T extends Resource> void register(T resource);
    <T extends Resource> void unregister(ResourceId id);
    <T extends Resource> Optional<T> find(ResourceId id, Class<T> type);
    <T extends Resource> Optional<T> findByName(String name, Class<T> type);
    <T extends Resource> List<T> findAll(Class<T> type);
    <T extends Resource> List<T> findByType(ResourceType type, Class<T> typeClass);
    boolean exists(ResourceId id);
    boolean existsByName(String name);
}