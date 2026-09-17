package tech.kayys.wayang.spi.capability;

/**
 * Strategy used by CapabilityRouter to select among multiple candidate providers.
 */
public enum CapabilityRoutingStrategy {

    /**
     * Explicitly routes to preferredProviderId requested by caller.
     */
    DIRECT,

    /**
     * Selects the first available and usable provider.
     */
    FIRST_AVAILABLE,

    /**
     * Prefers local/in-process providers over remote providers.
     */
    LOCAL_FIRST,

    /**
     * Prefers remote/distributed providers over local providers.
     */
    REMOTE_FIRST,

    /**
     * Selects based on declared priority attribute in capability descriptor.
     */
    PRIORITY,

    /**
     * Selects the provider reporting lowest load (0.0 .. 1.0).
     */
    LEAST_LOAD,

    /**
     * Selects proportionally based on weight / capacity attributes.
     */
    WEIGHTED
}
