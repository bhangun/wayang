package tech.kayys.wayang.extension;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Common metadata for all resources in Wayang.
 * Immutable and thread-safe.
 */
public record Metadata(
    String name,
    String description,
    Version version,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy,
    Map<String, String> labels,
    Map<String, Object> attributes
) {
    
    public static MetadataBuilder builder() {
        return new MetadataBuilder();
    }
    
    public static Metadata empty() {
        return new Metadata(
            null,
            null,
            Version.UNSPECIFIED,
            null,
            null,
            null,
            null,
            Collections.emptyMap(),
            Collections.emptyMap()
        );
    }
    
    public Metadata {
        labels = labels != null ? Collections.unmodifiableMap(new HashMap<>(labels)) : Collections.emptyMap();
        attributes = attributes != null ? Collections.unmodifiableMap(new HashMap<>(attributes)) : Collections.emptyMap();
    }
    
    public String getLabel(String key) {
        return labels.get(key);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) return null;
        return type.cast(value);
    }
    
    public boolean hasLabel(String key) {
        return labels.containsKey(key);
    }
    
    public Metadata withUpdatedAt(Instant timestamp) {
        return new Metadata(
            name,
            description,
            version,
            createdAt,
            timestamp,
            createdBy,
            updatedBy,
            labels,
            attributes
        );
    }
    
    public Metadata withUpdatedBy(String user) {
        return new Metadata(
            name,
            description,
            version,
            createdAt,
            updatedAt,
            createdBy,
            user,
            labels,
            attributes
        );
    }
    
    public static class MetadataBuilder {
        private String name;
        private String description;
        private Version version = Version.UNSPECIFIED;
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        private final Map<String, String> labels = new HashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();
        
        public MetadataBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public MetadataBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public MetadataBuilder version(Version version) {
            this.version = version;
            return this;
        }
        
        public MetadataBuilder version(String version) {
            this.version = Version.parse(version);
            return this;
        }
        
        public MetadataBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public MetadataBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public MetadataBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public MetadataBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }
        
        public MetadataBuilder label(String key, String value) {
            this.labels.put(key, value);
            return this;
        }
        
        public MetadataBuilder labels(Map<String, String> labels) {
            this.labels.putAll(labels);
            return this;
        }
        
        public MetadataBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
        
        public MetadataBuilder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }
        
        public MetadataBuilder now() {
            Instant now = Instant.now();
            this.createdAt = now;
            this.updatedAt = now;
            return this;
        }
        
        public Metadata build() {
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            if (updatedAt == null) {
                updatedAt = createdAt;
            }
            return new Metadata(
                name,
                description,
                version,
                createdAt,
                updatedAt,
                createdBy,
                updatedBy,
                labels,
                attributes
            );
        }
    }
}
