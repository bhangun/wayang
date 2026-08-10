package tech.kayys.wayang.spi.sandbox;

import tech.kayys.wayang.extension.Extension;

/**
 * Sandbox Provider - provides isolated execution environments
 */
public interface SandboxProvider extends Extension {
    
    /**
     * Returns the unique identifier of this sandbox provider (e.g., "docker", "nono").
     */
    String getProviderId();
    
    /**
     * Provisions a new sandbox instance based on the configuration.
     */
    Sandbox createSandbox(SandboxConfiguration config) throws Exception;
}
