package tech.kayys.wayang.configuration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.nio.file.*;
import java.util.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Configuration is a first-class Resource in Wayang.
 * 
 * This aligns with the meta-model: everything is a Resource.
 */
public interface ConfigurationResource extends Resource {
    
    ConfigId id();
    
    ResourceType type();
    
    ConfigType configType();
    
    String path();
    
    Object value();
    
    <T> T valueAs(Class<T> type);
    
    ConfigSource source();
    
    ConfigStatus status();
    
    Instant loadedAt();
    
    Instant lastModifiedAt();
    
    Map<String, Object> allValues();
    
    ConfigurationResource getSection(String path);
    
    boolean has(String path);
    
    <T> T get(String path, Class<T> type);
    
    <T> T get(String path, Class<T> type, T defaultValue);
    
    ConfigurationResource merge(ConfigurationResource other);
    
    ConfigurationResource withValue(String path, Object value);
    
    ConfigurationResource withStatus(ConfigStatus status);
    
    ConfigurationResource reload() throws Exception;
}

