package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityHealth;
import tech.kayys.wayang.spi.capability.CapabilityProviderRegistration;
import tech.kayys.wayang.spi.capability.CapabilityProviderStatus;
import tech.kayys.wayang.spi.capability.CapabilityRegistry;
import tech.kayys.wayang.spi.capability.CapabilityRoute;
import tech.kayys.wayang.spi.capability.CapabilityRouter;
import tech.kayys.wayang.spi.capability.CapabilityRoutingException;
import tech.kayys.wayang.spi.capability.CapabilityRoutingRequest;
import tech.kayys.wayang.spi.capability.CapabilityRoutingStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultCapabilityRouter implements CapabilityRouter {

    private final CapabilityRegistry registry;
    private final DefaultCapabilityHealthManager healthManager;

    public DefaultCapabilityRouter(
            CapabilityRegistry registry,
            DefaultCapabilityHealthManager healthManager) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.healthManager = healthManager;
    }

    @Override
    public CapabilityRoute route(CapabilityRoutingRequest request)
            throws CapabilityRoutingException {

        Objects.requireNonNull(request, "request cannot be null");

        List<CapabilityProviderRegistration> registered =
                registry.providersOf(request.capabilityId());

        if (registered.isEmpty()) {
            throw new CapabilityRoutingException(
                    "No provider registered for capability '" + request.capabilityId() + "'");
        }

        List<CapabilityProviderRegistration> usable = filterUsable(registered);

        if (usable.isEmpty()) {
            List<CapabilityProviderRegistration> degraded = filterDegraded(registered);
            if (!degraded.isEmpty()) {
                usable = degraded;
            } else {
                throw new CapabilityRoutingException(
                        "All providers for capability '" + request.capabilityId() + "' are unhealthy or unavailable");
            }
        }

        CapabilityRoutingStrategy strategy = request.strategy() != null
                ? request.strategy()
                : CapabilityRoutingStrategy.FIRST_AVAILABLE;

        return switch (strategy) {
            case DIRECT -> routeDirect(request, usable);
            case FIRST_AVAILABLE -> routeFirst(usable);
            case LOCAL_FIRST -> routeLocalFirst(usable);
            case REMOTE_FIRST -> routeRemoteFirst(usable);
            case PRIORITY -> routePriority(request, usable);
            case LEAST_LOAD -> routeLeastLoad(usable);
            case WEIGHTED -> routeWeighted(usable);
        };
    }

    private List<CapabilityProviderRegistration> filterUsable(
            List<CapabilityProviderRegistration> providers) {
        if (healthManager == null) {
            return providers;
        }

        return providers.stream()
                .filter(p -> {
                    Optional<CapabilityProviderStatus> status =
                            healthManager.statusOf(p.capabilityId(), p.providerId());
                    return status.map(CapabilityProviderStatus::usable).orElse(true);
                })
                .toList();
    }

    private List<CapabilityProviderRegistration> filterDegraded(
            List<CapabilityProviderRegistration> providers) {
        if (healthManager == null) {
            return List.of();
        }

        return providers.stream()
                .filter(p -> {
                    Optional<CapabilityProviderStatus> status =
                            healthManager.statusOf(p.capabilityId(), p.providerId());
                    return status.map(s -> s.available() && s.health() == CapabilityHealth.DEGRADED)
                            .orElse(false);
                })
                .toList();
    }

    private CapabilityRoute routeDirect(
            CapabilityRoutingRequest request,
            List<CapabilityProviderRegistration> providers) {

        if (request.preferredProviderId() == null) {
            return routeFirst(providers);
        }

        return providers.stream()
                .filter(p -> p.providerId().equals(request.preferredProviderId()))
                .findFirst()
                .map(this::toRoute)
                .orElseThrow(() -> new CapabilityRoutingException(
                        "Preferred provider '" + request.preferredProviderId()
                                + "' is not available for capability '" + request.capabilityId() + "'"));
    }

    private CapabilityRoute routeFirst(List<CapabilityProviderRegistration> providers) {
        return toRoute(providers.getFirst());
    }

    private CapabilityRoute routeLocalFirst(List<CapabilityProviderRegistration> providers) {
        return toRoute(providers.stream()
                .filter(this::isLocal)
                .findFirst()
                .orElseGet(providers::getFirst));
    }

    private CapabilityRoute routeRemoteFirst(List<CapabilityProviderRegistration> providers) {
        return toRoute(providers.stream()
                .filter(p -> !isLocal(p))
                .findFirst()
                .orElseGet(providers::getFirst));
    }

    private CapabilityRoute routePriority(
            CapabilityRoutingRequest request,
            List<CapabilityProviderRegistration> providers) {

        return toRoute(providers.stream()
                .sorted(Comparator.comparingInt(this::priority).reversed())
                .findFirst()
                .orElseGet(providers::getFirst));
    }

    private CapabilityRoute routeLeastLoad(List<CapabilityProviderRegistration> providers) {
        if (healthManager == null || providers.size() <= 1) {
            return toRoute(providers.getFirst());
        }

        return toRoute(providers.stream()
                .min(Comparator.comparingDouble(p -> {
                    Optional<CapabilityProviderStatus> status =
                            healthManager.statusOf(p.capabilityId(), p.providerId());
                    return status.map(s -> s.load() != null ? s.load() : 0.0).orElse(0.0);
                }))
                .orElseGet(providers::getFirst));
    }

    private CapabilityRoute routeWeighted(List<CapabilityProviderRegistration> providers) {
        return routeLeastLoad(providers);
    }

    private boolean isLocal(CapabilityProviderRegistration provider) {
        Object execution = provider.capability().descriptor().attributes().get("execution");
        return "local".equals(execution) || "in-process".equals(execution);
    }

    private int priority(CapabilityProviderRegistration provider) {
        Object val = provider.capability().descriptor().attributes().get("priority");
        if (val instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    private CapabilityRoute toRoute(CapabilityProviderRegistration provider) {
        return new CapabilityRoute(
                provider.capabilityId(),
                provider.providerId(),
                provider.capability()
        );
    }
}
