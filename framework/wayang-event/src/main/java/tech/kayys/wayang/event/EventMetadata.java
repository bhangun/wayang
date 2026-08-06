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


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Event Metadata - additional event context
 */
public record EventMetadata(
    String tenant,
    String namespace,
    String user,
    String session,
    Map<String, String> labels
) {
    public static EventMetadata empty() {
        return new EventMetadata(null, null, null, null, Collections.emptyMap());
    }
    
    public static EventMetadata of(String tenant, String user) {
        return new EventMetadata(tenant, null, user, null, Collections.emptyMap());
    }
    
    public EventMetadata withLabel(String key, String value) {
        Map<String, String> newLabels = new HashMap<>(labels);
        newLabels.put(key, value);
        return new EventMetadata(tenant, namespace, user, session, newLabels);
    }
}
