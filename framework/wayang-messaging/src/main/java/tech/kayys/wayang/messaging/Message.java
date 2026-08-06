package tech.kayys.wayang.messaging;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Message
 */
public record Message(
    String id,
    String key,
    Object payload,
    Map<String, Object> headers,
    Instant timestamp
) {
    public static Message of(Object payload) {
        return new Message(
            Id.random().asString(),
            null,
            payload,
            Map.of(),
            Instant.now()
        );
    }
    
    public static Message of(String key, Object payload) {
        return new Message(
            Id.random().asString(),
            key,
            payload,
            Map.of(),
            Instant.now()
        );
    }
    
    public Message withHeader(String key, Object value) {
        Map<String, Object> newHeaders = new HashMap<>(headers);
        newHeaders.put(key, value);
        return new Message(id, this.key, payload, newHeaders, timestamp);
    }
}