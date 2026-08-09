package tech.kayys.wayang.spi.sandbox;

import tech.kayys.wayang.spi.plugin.ExtensionPoint;

/**
 * Service Provider Interface for Sandbox implementations.
 */
public interface SandboxProvider extends ExtensionPoint {
    
    /**
     * Returns the unique identifier of this sandbox provider (e.g., "docker", "nono").
     */
    String getProviderId();
    
    /**
     * Provisions a new sandbox instance based on the configuration.
     */
    Sandbox createSandbox(SandboxConfiguration config) throws Exception;
}
