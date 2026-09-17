package tech.kayys.wayang.spi.capability;

import java.util.List;
import java.util.Optional;

/**
 * Authoritative lookup and registration index for capabilities across all providers.
 */
public interface CapabilityRegistry {

    /**
     * Checks if any provider offers the specified capability ID.
     */
    boolean contains(String capabilityId);

    /**
     * Finds the default/first capability instance for an ID.
     */
    Optional<Capability> find(String capabilityId);

    /**
     * Returns all registered capabilities across all providers.
     */
    List<Capability> findAll();

    /**
     * Finds capabilities matching the specified type.
     */
    List<Capability> findByType(CapabilityType type);

    /**
     * Finds the default provider ID for a capability.
     */
    Optional<String> providerOf(String capabilityId);

    /**
     * Lists all capability IDs registered by a specific provider (plugin).
     */
    List<String> capabilitiesOf(String providerId);

    /**
     * Lists all registrations offering the specified capability ID.
     */
    List<CapabilityProviderRegistration> providersOf(String capabilityId);

    /**
     * Returns all registrations in the registry.
     */
    default List<CapabilityProviderRegistration> allProviderRegistrations() {
        return findAll().stream()
                .flatMap(c -> providersOf(c.id()).stream())
                .distinct()
                .toList();
    }
}
