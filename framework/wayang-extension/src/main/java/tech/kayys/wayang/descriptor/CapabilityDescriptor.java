package tech.kayys.wayang.descriptor;
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

/**
 * Describes a capability of a resource.
 */
public record CapabilityDescriptor(
    String name,
    String description,
    CapabilityType type,
    Set<String> features,
    Map<String, Object> properties,
    CapabilityLevel level
) {
    
    public static CapabilityDescriptor of(String name, CapabilityType type) {
        return new CapabilityDescriptor(
            name,
            null,
            type,
            Collections.emptySet(),
            Collections.emptyMap(),
            CapabilityLevel.STANDARD
        );
    }
    
    public static CapabilityDescriptor of(String name, CapabilityType type, CapabilityLevel level) {
        return new CapabilityDescriptor(
            name,
            null,
            type,
            Collections.emptySet(),
            Collections.emptyMap(),
            level
        );
    }
    
    public boolean hasFeature(String feature) {
        return features.contains(feature);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value == null) return null;
        return type.cast(value);
    }
}