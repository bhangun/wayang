package tech.kayys.wayang.spi.output;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.Artifact;


/**
 * Output Result - complete result model
 */
public record OutputResult(
    String id,
    OutputType type,
    String content,
    List<Artifact> artifacts,
    Map<String, Object> metadata,
    boolean isComplete,
    Instant timestamp,
    String sessionId,
    String recipient
) {
    public static OutputResult of(String content) {
        return new OutputResult(
            Id.random().asString(),
            OutputType.TEXT,
            content,
            List.of(),
            Map.of(),
            true,
            Instant.now(),
            null,
            null
        );
    }
    
    public static OutputResult of(String content, OutputType type) {
        return new OutputResult(
            Id.random().asString(),
            type,
            content,
            List.of(),
            Map.of(),
            true,
            Instant.now(),
            null,
            null
        );
    }
    
    public OutputResult withArtifact(Artifact artifact) {
        List<Artifact> newArtifacts = new ArrayList<>(artifacts);
        newArtifacts.add(artifact);
        return new OutputResult(id, type, content, newArtifacts, metadata, 
            isComplete, timestamp, sessionId, recipient);
    }
    
    public OutputResult withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(metadata);
        newMetadata.put(key, value);
        return new OutputResult(id, type, content, artifacts, newMetadata, 
            isComplete, timestamp, sessionId, recipient);
    }
}
