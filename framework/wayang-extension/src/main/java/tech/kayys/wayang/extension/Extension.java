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


import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Version;
import java.util.Set;

/**
 * Base extension interface - all plugins implement this.
 * 
 * <p>Extensions are the primary way to extend Wayang functionality.
 * Each extension represents a pluggable component that can be loaded
 * dynamically at runtime.</p>
 * 
 * <h2>Extension Lifecycle</h2>
 * <pre>
 * ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
 * │  DISCOVERED │───▶│  INITIALIZED│───▶│   STARTED   │───▶│   ACTIVE   │
 * └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
 *        │                  │                  │                  │
 *        ▼                  ▼                  ▼                  ▼
 * ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
 * │   STOPPED   │◀───│   ERROR     │◀───│  SHUTDOWN   │◀───│   PAUSED   │
 * └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
 * </pre>
 */
public interface Extension extends Resource {
    
    /**
     * Gets the extension's unique identifier as a string.
     * 
     * <p>This is a convenience method that returns the string representation
     * of the Resource's ID. It is equivalent to {@code id().asString()}.</p>
     * 
     * @return The extension's ID as a string
     */
    default String getId() {
        return id().asString();
    }
    
    /**
     * Gets the extension's display name.
     * 
     * @return The extension name
     */
    default String getName() {
        return metadata().name();
    }
    
    /**
     * Gets the extension's version.
     * 
     * @return The extension version
     */
    default String getVersionAsString() {
        return metadata().version().toString();
    }
    
    /**
     * Gets the extension's description.
     * 
     * @return The extension description
     */
    default String getDescription() {
        return metadata().description();
    }
    
    /**
     * Gets the capabilities this extension supports.
     * 
     * @return Set of supported capabilities
     */
    default Set<Capability> capabilities() {
        return Set.of();
    }
    
    /**
     * Checks if this extension supports a specific capability.
     * 
     * @param capability The capability to check
     * @return true if supported
     */
    default boolean supports(Capability capability) {
        return capabilities().contains(capability);
    }
    
    /**
     * Initializes the extension.
     * 
     * <p>Called when the extension is loaded and before it becomes active.
     * This method should perform any necessary setup, such as:</p>
     * <ul>
     *   <li>Loading configuration</li>
     *   <li>Establishing connections</li>
     *   <li>Creating resources</li>
     *   <li>Registering with services</li>
     * </ul>
     * 
     * @throws Exception If initialization fails
     */
    default void initialize() throws Exception {
        // Hook for initialization
    }
    
    /**
     * Shuts down the extension.
     * 
     * <p>Called when the extension is being unloaded or when the system is
     * shutting down. This method should clean up resources, such as:</p>
     * <ul>
     *   <li>Closing connections</li>
     *   <li>Releasing resources</li>
     *   <li>Unregistering services</li>
     *   <li>Persisting state</li>
     * </ul>
     * 
     * @throws Exception If shutdown fails
     */
    default void shutdown() throws Exception {
        // Hook for shutdown
    }
}
