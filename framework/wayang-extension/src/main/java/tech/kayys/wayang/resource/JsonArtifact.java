package tech.kayys.wayang.resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

import tech.kayys.wayang.artifact.ArtifactFormat;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.identity.ResourceId.ArtifactId;

/**
 * JSON Artifact
 */
public final record JsonArtifact(
    ArtifactId id,
    ResourceType type,
    ArtifactFormat format,
    String json,
    Object parsed,
    Metadata metadata,
    Map<String, Object> artifactMetadata
) implements Artifact {
    
    public JsonArtifact {
        Objects.requireNonNull(json, "json cannot be null");
        if (metadata == null) {
            metadata = Metadata.builder()
                .name("json-artifact")
                .label("type", type.name())
                .label("format", format.name())
                .now()
                .build();
        }
        if (artifactMetadata == null) {
            artifactMetadata = Collections.emptyMap();
        }
    }
    
    public static JsonArtifact of(String json) {
        return new JsonArtifact(
            new ResourceId.ArtifactId(Id.random()),
            ArtifactType.JSON,
            ArtifactFormat.JSON,
            json,
            null,
            null,
            Map.of()
        );
    }
    
    public static JsonArtifact of(String json, Object parsed) {
        return new JsonArtifact(
            new ResourceId.ArtifactId(Id.random()),
            ArtifactType.JSON,
            ArtifactFormat.JSON,
            json,
            parsed,
            null,
            Map.of()
        );
    }
    
    @Override
    public ArtifactId id() { return id; }
    
    @Override
    public ResourceType type() { return type; }
    
    @Override
    public ArtifactFormat format() { return format; }
    
    @Override
    public Object content() { return parsed != null ? parsed : json; }
    
    @Override
    public String asString() { return json; }
    
    @Override
    public byte[] asBytes() {
        return json.getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public tech.kayys.wayang.extension.Metadata metadata() { return metadata; }
    
    
}