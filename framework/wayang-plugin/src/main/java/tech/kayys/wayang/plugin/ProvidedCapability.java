package tech.kayys.wayang.plugin;
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

import tech.kayys.wayang.extension.Version;

/**
 * Provided Capability
 */
public record ProvidedCapability(
    String id,
    String name,
    Version version,
    String type,
    String description,
    List<String> features,
    Map<String, Object> metadata
) {
    public static ProvidedCapability of(String id, String type) {
        return new ProvidedCapability(id, id, Version.VERSION_1_0_0, type, null, List.of(), Map.of());
    }
    
    public static ProvidedCapability of(String id, String name, String type) {
        return new ProvidedCapability(id, name, Version.VERSION_1_0_0, type, null, List.of(), Map.of());
    }
    
    public static ProvidedCapability of(String id, String name, Version version, String type) {
        return new ProvidedCapability(id, name, version, type, null, List.of(), Map.of());
    }
}