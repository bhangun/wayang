package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityProviderRegistration;
import tech.kayys.wayang.spi.capability.CapabilityRegistry;
import tech.kayys.wayang.spi.capability.CapabilityType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultCapabilityRegistry implements CapabilityRegistry {

    private final ConcurrentMap<String, ConcurrentMap<String, CapabilityProviderRegistration>> capabilities =
            new ConcurrentHashMap<>();

    public void register(
            String capabilityId,
            String providerId,
            Capability capability) {

        Objects.requireNonNull(capabilityId, "capabilityId cannot be null");
        Objects.requireNonNull(providerId, "providerId cannot be null");
        Objects.requireNonNull(capability, "capability cannot be null");

        ConcurrentMap<String, CapabilityProviderRegistration> providers =
                capabilities.computeIfAbsent(capabilityId, ignored -> new ConcurrentHashMap<>());

        CapabilityProviderRegistration registration =
                new CapabilityProviderRegistration(capabilityId, providerId, capability);

        CapabilityProviderRegistration previous = providers.putIfAbsent(providerId, registration);
        if (previous != null) {
            throw new IllegalStateException(
                    "Capability '" + capabilityId + "' is already registered by provider '" + providerId + "'");
        }
    }

    public void unregister(String capabilityId, String providerId) {
        if (capabilityId == null || providerId == null) {
            return;
        }

        ConcurrentMap<String, CapabilityProviderRegistration> providers = capabilities.get(capabilityId);
        if (providers == null) {
            return;
        }

        providers.remove(providerId);
        if (providers.isEmpty()) {
            capabilities.remove(capabilityId, providers);
        }
    }

    public void unregisterAll(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return;
        }

        for (var entry : capabilities.entrySet()) {
            unregister(entry.getKey(), providerId);
        }
    }

    @Override
    public boolean contains(String capabilityId) {
        if (capabilityId == null || capabilityId.isBlank()) {
            return false;
        }
        ConcurrentMap<String, CapabilityProviderRegistration> providers = capabilities.get(capabilityId);
        return providers != null && !providers.isEmpty();
    }

    @Override
    public Optional<Capability> find(String capabilityId) {
        return providersOf(capabilityId).stream()
                .findFirst()
                .map(CapabilityProviderRegistration::capability);
    }

    @Override
    public List<Capability> findAll() {
        return capabilities.values().stream()
                .flatMap(providers -> providers.values().stream())
                .map(CapabilityProviderRegistration::capability)
                .distinct()
                .toList();
    }

    @Override
    public List<Capability> findByType(CapabilityType type) {
        if (type == null) {
            return List.of();
        }

        return findAll().stream()
                .filter(capability -> type.equals(capability.type()))
                .toList();
    }

    @Override
    public Optional<String> providerOf(String capabilityId) {
        return providersOf(capabilityId).stream()
                .findFirst()
                .map(CapabilityProviderRegistration::providerId);
    }

    @Override
    public List<String> capabilitiesOf(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return List.of();
        }

        return capabilities.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(providerId))
                .map(java.util.Map.Entry::getKey)
                .sorted()
                .toList();
    }

    @Override
    public List<CapabilityProviderRegistration> providersOf(String capabilityId) {
        if (capabilityId == null || capabilityId.isBlank()) {
            return List.of();
        }

        ConcurrentMap<String, CapabilityProviderRegistration> providers = capabilities.get(capabilityId);
        if (providers == null) {
            return List.of();
        }

        return providers.values().stream()
                .sorted(Comparator.comparing(CapabilityProviderRegistration::providerId))
                .toList();
    }

    @Override
    public List<CapabilityProviderRegistration> allProviderRegistrations() {
        return capabilities.values().stream()
                .flatMap(m -> m.values().stream())
                .distinct()
                .toList();
    }
}
