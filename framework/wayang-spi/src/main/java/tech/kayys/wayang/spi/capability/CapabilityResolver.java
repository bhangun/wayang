package tech.kayys.wayang.spi.capability;

/**
 * Resolves capability requirements against registered providers.
 */
public interface CapabilityResolver {

    /**
     * Resolves requirements against the registry.
     */
    CapabilityResolutionResult resolve(CapabilityRequirements requirements);
}
