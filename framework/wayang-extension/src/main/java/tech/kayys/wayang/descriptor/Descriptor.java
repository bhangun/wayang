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

import tech.kayys.wayang.resource.Resource;

/**
 * Descriptor pattern for all resources.
 * Provides metadata about resources for discovery and UI generation.
 */
public interface Descriptor extends Resource {
    
    tech.kayys.wayang.identity.ResourceId id();
    
    String name();
    
    String version();
    
    String description();
    
    Set<String> tags();
    
    Set<String> categories();
    
    Map<String, ParameterDescriptor> inputs();
    
    Map<String, ParameterDescriptor> outputs();
    
    List<CapabilityDescriptor> capabilities();
    
    
    
    default boolean hasTag(String tag) {
        return tags().contains(tag);
    }
    
    default boolean hasCategory(String category) {
        return categories().contains(category);
    }
}
