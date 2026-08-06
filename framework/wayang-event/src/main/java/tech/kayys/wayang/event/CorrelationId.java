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
 * Correlation ID - groups events from the same execution
 */
public final record CorrelationId(Id value) implements ResourceId {
    public static CorrelationId random() {
        return new CorrelationId(Id.random());
    }
    
    public static CorrelationId fromString(String value) {
        return new CorrelationId(Id.fromString(value));
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Custom("correlation");
    }
    
    @Override
    public String asString() {
        return value.asString();
    }
}
