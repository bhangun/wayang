package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityRegistry;
import tech.kayys.wayang.spi.capability.CapabilityRequirement;
import tech.kayys.wayang.spi.capability.CapabilityRequirements;
import tech.kayys.wayang.spi.capability.CapabilityResolutionResult;
import tech.kayys.wayang.spi.capability.CapabilityResolver;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class DefaultCapabilityResolver implements CapabilityResolver {

    private final CapabilityRegistry registry;

    public DefaultCapabilityResolver(CapabilityRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
    }

    @Override
    public CapabilityResolutionResult resolve(CapabilityRequirements requirements) {
        Objects.requireNonNull(requirements, "requirements cannot be null");

        if (requirements.isEmpty()) {
            return CapabilityResolutionResult.success(Map.of());
        }

        Map<String, Capability> resolved = new LinkedHashMap<>();
        Set<String> missing = new LinkedHashSet<>();
        boolean satisfied = true;

        for (CapabilityRequirement req : requirements.requirements()) {
            Optional<Capability> found = registry.find(req.capabilityId());

            if (found.isPresent()) {
                resolved.put(req.capabilityId(), found.get());
            } else {
                if (req.required()) {
                    missing.add(req.capabilityId());
                    satisfied = false;
                }
            }
        }

        return CapabilityResolutionResult.partial(resolved, missing, satisfied);
    }
}
