package tech.kayys.wayang.guard;
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
 * Violation
 */
public record Violation(
    String id,
    String type,
    String description,
    String severity,
    String location,
    Map<String, Object> context
) {
    public static Violation of(String type, String description) {
        return new Violation(
            Id.random().asString(),
            type,
            description,
            "WARNING",
            null,
            Map.of()
        );
    }
    
    public static Violation of(String type, String description, String severity) {
        return new Violation(
            Id.random().asString(),
            type,
            description,
            severity,
            null,
            Map.of()
        );
    }
}
