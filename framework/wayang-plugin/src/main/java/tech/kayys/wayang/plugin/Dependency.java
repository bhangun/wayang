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


import java.util.Objects;

import tech.kayys.wayang.extension.Version;

/**
 * Dependency in a manifest
 */
public record Dependency(
    String id,
    Version version,
    String scope
) {
    public Dependency {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(version, "version cannot be null");
    }
    
    public static Dependency of(String id, String version) {
        return new Dependency(id, Version.parse(version), null);
    }
    
    public static Dependency of(String id, Version version) {
        return new Dependency(id, version, null);
    }
}
