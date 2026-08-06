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


import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Configuration ID - strongly typed
 */
public final record ConfigId(Id value) implements ResourceId {
    public static ConfigId random() {
        return new ConfigId(Id.random());
    }
    
    public static ConfigId fromString(String value) {
        return new ConfigId(Id.fromString(value));
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Configuration();
    }
    
    @Override
    public String asString() {
        return value.asString();
    }
}
