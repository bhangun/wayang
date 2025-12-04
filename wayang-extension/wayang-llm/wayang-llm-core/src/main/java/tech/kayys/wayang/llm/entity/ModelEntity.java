package tech.kayys.wayang.models.core.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Model metadata entity stored in PostgreSQL.
 */
@Entity
@Table(name = "models", indexes = {
    @Index(name = "idx_model_provider", columnList = "provider"),
    @Index(name = "idx_model_status", columnList = "status"),
    @Index(name = "idx_model_type", columnList = "type")
})
@Getter
@Setter
public class ModelEntity extends PanacheEntityBase {
    
    @Id
    @Column(name = "model_id", length = 255)
    private String modelId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private String provider;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(name = "capabilities", columnDefinition = "text[]")
    private String[] capabilities;
    
    @Column(name = "max_tokens")
    private Integer maxTokens;
    
    @Column(name = "max_output_tokens")
    private Integer maxOutputTokens;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "latency_profile", columnDefinition = "jsonb")
    private Map<String, Object> latencyProfile;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cost_profile", columnDefinition = "jsonb")
    private Map<String, Object> costProfile;
    
    @Column(name = "supported_languages", columnDefinition = "text[]")
    private String[] supportedLanguages;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(columnDefinition = "text[]")
    private String[] tags;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
    
    @Column(length = 500)
    private String endpoint;
    
    @Column(nullable = false, length = 50)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(length = 255)
    private String owner;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // Custom queries
    public static Uni<List<ModelEntity>> findByProvider(String provider) {
        return list("provider = ?1 and status = 'ACTIVE'", provider);
    }

    public static Uni<List<ModelEntity>> findByCapabilities(Set<String> capabilities) {
        return list("capabilities @> ?1 and status = 'ACTIVE'", 
            capabilities.toArray(new String[0]));
    }

    public static Uni<List<ModelEntity>> findActive() {
        return list("status", "ACTIVE");
    }
}