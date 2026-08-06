package tech.kayys.wayang.input;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.Artifact;

/**
 * Input Result
 */
public record InputResult(
    String id,
    InputType type,
    String content,
    Map<String, Object> metadata,
    List<Artifact> artifacts,
    boolean isComplete
) {
    public static InputResult of(String content) {
        return new InputResult(
            Id.random().asString(),
            InputType.TEXT,
            content,
            Map.of(),
            List.of(),
            true
        );
    }
    
    public static InputResult of(String content, InputType type) {
        return new InputResult(
            Id.random().asString(),
            type,
            content,
            Map.of(),
            List.of(),
            true
        );
    }
}