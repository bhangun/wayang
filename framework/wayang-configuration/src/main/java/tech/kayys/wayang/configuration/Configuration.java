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


import java.util.List;
import java.util.Map;


/**
 * Complete Configuration System for Wayang
 */
public interface Configuration {
    
    <T> T get(String path, Class<T> type);
    
    <T> T get(String path, Class<T> type, T defaultValue);
    
    boolean has(String path);
    
    Configuration getSection(String path);
    
    List<String> getKeys();
    
    Map<String, Object> asMap();
    
    void reload() throws Exception;
    
    void watch(ConfigurationListener listener);
    
    void unwatch(ConfigurationListener listener);
    
    default String getString(String path) {
        return get(path, String.class);
    }
    
    default String getString(String path, String defaultValue) {
        return get(path, String.class, defaultValue);
    }
    
    default int getInt(String path) {
        return get(path, Integer.class);
    }
    
    default int getInt(String path, int defaultValue) {
        return get(path, Integer.class, defaultValue);
    }
    
    default long getLong(String path) {
        return get(path, Long.class);
    }
    
    default long getLong(String path, long defaultValue) {
        return get(path, Long.class, defaultValue);
    }
    
    default boolean getBoolean(String path) {
        return get(path, Boolean.class);
    }
    
    default boolean getBoolean(String path, boolean defaultValue) {
        return get(path, Boolean.class, defaultValue);
    }
    
    default double getDouble(String path) {
        return get(path, Double.class);
    }
    
    default double getDouble(String path, double defaultValue) {
        return get(path, Double.class, defaultValue);
    }
    
    default List<String> getStringList(String path) {
        return get(path, List.class);
    }
    
    default Map<String, Object> getMap(String path) {
        return get(path, Map.class);
    }
}