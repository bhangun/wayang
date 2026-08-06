package tech.kayys.wayang.inference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Attachment
 */
public record Attachment(
    String id,
    String type,
    String name,
    byte[] data,
    String url,
    Map<String, Object> metadata
) {
    public static Attachment of(String type, String name, byte[] data) {
        return new Attachment(
            Id.random().asString(),
            type,
            name,
            data,
            null,
            Map.of()
        );
    }
    
    public static Attachment ofUrl(String type, String name, String url) {
        return new Attachment(
            Id.random().asString(),
            type,
            name,
            null,
            url,
            Map.of()
        );
    }
}