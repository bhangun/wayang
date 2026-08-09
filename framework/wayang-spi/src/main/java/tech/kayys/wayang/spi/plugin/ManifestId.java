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


import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Manifest ID - strongly typed
 */
public final record ManifestId(Id value) implements ResourceId {
    public static ManifestId random() {
        return new ManifestId(Id.random());
    }
    
    public static ManifestId fromString(String value) {
        return new ManifestId(Id.fromString(value));
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Manifest();
    }
    
    @Override
    public String asString() {
        return value.asString();
    }
}
