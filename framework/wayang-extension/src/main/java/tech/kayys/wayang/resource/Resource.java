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


import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.identity.ResourceId;


import java.time.Instant;

/**
 * The universal resource interface.
 * Everything in Wayang is a Resource.
 * 
 * <p>All entities in the Wayang system are represented as Resources.
 * This provides a unified way to:</p>
 * <ul>
 *   <li>Identify entities (id())</li>
 *   <li>Type entities (type())</li>
 *   <li>Manage metadata (metadata())</li>
 *   <li>Track lifecycle (createdAt, updatedAt)</li>
 *   <li>Apply labels and attributes</li>
 * </ul>
 */
public interface Resource {
    
    /**
     * Gets the unique identifier of this resource.
     * 
     * @return The resource ID
     */
    ResourceId id();
    
    /**
     * Gets the type of this resource.
     * 
     * @return The resource type
     */
    ResourceType type();
    
    /**
     * Gets the metadata of this resource.
     * 
     * @return The resource metadata
     */
    Metadata metadata();
    
    /**
     * Gets the foundation ID value.
     * 
     * @return The ID value
     */
    default Id getFoundationId() {
        return id().value();
    }
    
    /**
     * Gets the resource name.
     * 
     * @return The name
     */
    default String getName() {
        return metadata().name();
    }
    
    /**
     * Gets the resource description.
     * 
     * @return The description
     */
    default String getDescription() {
        return metadata().description();
    }
    
    /**
     * Gets the resource version.
     * 
     * @return The version
     */
    default Version getVersion() {
        return metadata().version();
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return The creation time
     */
    default Instant getCreatedAt() {
        return metadata().createdAt();
    }
    
    /**
     * Gets the last update timestamp.
     * 
     * @return The last update time
     */
    default Instant getUpdatedAt() {
        return metadata().updatedAt();
    }
    
    /**
     * Checks if a label exists.
     * 
     * @param key The label key
     * @return true if the label exists
     */
    default boolean hasLabel(String key) {
        return metadata().hasLabel(key);
    }
    
    /**
     * Gets a label value.
     * 
     * @param key The label key
     * @return The label value, or null if not found
     */
    default String getLabel(String key) {
        return metadata().getLabel(key);
    }
}