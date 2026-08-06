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
 * Tool for function calling
 */
public record Tool(
    String id,
    String name,
    String description,
    Map<String, Object> parameters,
    Map<String, Object> metadata
) {
    public static Tool of(String name, String description, Map<String, Object> parameters) {
        return new Tool(
            Id.random().asString(),
            name,
            description,
            parameters,
            Map.of()
        );
    }
}