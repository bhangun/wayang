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


import tech.kayys.wayang.extension.Version;

/**
 * Required Capability
 */
public record RequiredCapability(
    String id,
    Version version,
    String type,
    boolean optional,
    String description
) {
    public static RequiredCapability of(String id, String type) {
        return new RequiredCapability(id, Version.VERSION_1_0_0, type, false, null);
    }
    
    public static RequiredCapability of(String id, Version version, String type) {
        return new RequiredCapability(id, version, type, false, null);
    }
    
    public RequiredCapability optional(boolean optional) {
        return new RequiredCapability(id, version, type, optional, description);
    }
}