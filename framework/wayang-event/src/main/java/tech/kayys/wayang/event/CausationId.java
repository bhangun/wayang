package tech.kayys.wayang.event;
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
 * Causation ID - identifies which event caused this event
 */
public final record CausationId(Id value) implements ResourceId {
    public static CausationId random() {
        return new CausationId(Id.random());
    }
    
    public static CausationId fromString(String value) {
        return new CausationId(Id.fromString(value));
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Custom("causation");
    }
    
    @Override
    public String asString() {
        return value.asString();
    }
}
