package tech.kayys.wayang.spi.plugin;
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
import java.nio.file.*;
import java.io.*;
import java.net.*;
import java.util.jar.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.*;

import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.spi.plugin.Dependency;
import tech.kayys.wayang.resource.Resource;

/**
 * Manifest - describes a plugin or extension.
 * 
 * This is a first-class Resource in Wayang.
 */
public interface Manifest extends Resource {
    
    ManifestId id();
    
    String name();
    
    Version version();
    
    String description();
    
    String mainClass();
    
    List<Dependency> dependencies();
    
    List<ProvidedCapability> provides();
    
    List<RequiredCapability> requires();
    
    List<Permission> permissions();
    
    Map<String, Object> configuration();
    
    List<String> authors();
    
    String license();
    
    String repository();
    
    String documentation();
    
    tech.kayys.wayang.extension.Metadata metadata();
    
    ManifestStatus status();
    
    Path location();
    
    Manifest withStatus(ManifestStatus status);
    
    default boolean hasDependency(String id) {
        return dependencies().stream().anyMatch(d -> d.id().equals(id));
    }
    
    default Optional<Dependency> findDependency(String id) {
        return dependencies().stream().filter(d -> d.id().equals(id)).findFirst();
    }
    
    default boolean provides(String capability) {
        return provides().stream().anyMatch(p -> p.id().equals(capability));
    }
    
    default boolean requires(String capability) {
        return requires().stream().anyMatch(r -> r.id().equals(capability));
    }
}
