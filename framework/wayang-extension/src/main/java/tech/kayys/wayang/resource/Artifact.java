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
import java.util.*;

import tech.kayys.wayang.artifact.ArtifactFormat;
import tech.kayys.wayang.identity.ResourceId.ArtifactId;

/**
 * Artifact - everything produced during execution.
 */
public interface Artifact extends Resource {
    
    ArtifactId id();
    
    ResourceType type();
    
    ArtifactFormat format();
    
    Object content();
    
    String asString();
    
    byte[] asBytes();
    
    tech.kayys.wayang.extension.Metadata metadata();
    
    default ArtifactMetadata artifactMetadata() {
        return null;
    }
    
    default boolean isText() {
        return type() == ArtifactType.TEXT;
    }
    
    default boolean isJson() {
        return format() == ArtifactFormat.JSON;
    }
    
    default boolean isStreaming() {
        return format() == ArtifactFormat.STREAM;
    }
}
