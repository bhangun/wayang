package tech.kayys.wayang.resource;

import java.time.Instant;

import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.definition.Definition;

import tech.kayys.wayang.identity.ResourceId;
import java.util.Objects;

/**
 * Base implementation of a resource that can be extended.
 * 
 * <p>This class provides a convenient base for implementing the
 * {@link Resource} interface. It handles the common fields and
 * provides implementations of {@code equals()}, {@code hashCode()},
 * and {@code toString()}.</p>
 * 
 * <h2>Usage</h2>
 * <pre>
 * {@code
 * public class MyResource extends BaseResource {
 *     public MyResource(ResourceId id, Metadata metadata) {
 *         super(id, metadata);
 *     }
 * }
 * }
 * </pre>
 */
public abstract class BaseResource implements Resource 
    {
    
    private final ResourceId id;
    private final Metadata metadata;
    
    /**
     * Creates a new BaseResource with the given ID and metadata.
     * 
     * @param id The resource ID (must not be null)
     * @param metadata The resource metadata (if null, empty metadata will be used)
     * @throws NullPointerException If id is null
     */
    protected BaseResource(ResourceId id, Metadata metadata) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.metadata = metadata != null ? metadata : Metadata.empty();
    }
    
    @Override
    public ResourceId id() {
        return id;
    }
    
    @Override
    public ResourceType type() {
        return id.type();
    }
    
    @Override
    public Metadata metadata() {
        return metadata;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseResource that = (BaseResource) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s]", 
            getClass().getSimpleName(), 
            id.asString(), 
            metadata.name()
        );
    }
}