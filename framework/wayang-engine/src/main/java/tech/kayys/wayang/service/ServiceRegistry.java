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

/**
 * Service Registry - discovers and manages services
 */
public interface ServiceRegistry {
    <T> void register(Class<T> serviceType, T instance);
    <T> void unregister(Class<T> serviceType);
    <T> Optional<T> get(Class<T> serviceType);
    <T> List<T> getAll(Class<T> serviceType);
    boolean has(Class<?> serviceType);
}