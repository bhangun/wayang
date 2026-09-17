package tech.kayys.wayang.spi.capability;

import java.util.List;

/**
 * Implemented by plugins or extensions that provide one or more capabilities.
 */
public interface CapabilityProvider {

    /**
     * Unique identifier of the provider (typically the owning plugin ID).
     */
    String providerId();

    /**
     * Capabilities supplied by this provider.
     */
    List<Capability> capabilities();
}
