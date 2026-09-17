package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.CapabilityAvailability;
import tech.kayys.wayang.spi.capability.CapabilityHealth;
import tech.kayys.wayang.spi.capability.CapabilityHealthChecker;
import tech.kayys.wayang.spi.capability.CapabilityProviderRegistration;
import tech.kayys.wayang.spi.capability.CapabilityProviderStatus;
import tech.kayys.wayang.spi.capability.CapabilityRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultCapabilityHealthManager {

    private final CapabilityRegistry registry;
    private final CapabilityHealthChecker defaultChecker;
    private final ConcurrentMap<String, CapabilityHealthChecker> customCheckers =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, ConcurrentMap<String, CapabilityProviderStatus>> statuses =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Integer> failureCounters =
            new ConcurrentHashMap<>();

    private static final int DEGRADED_FAILURE_THRESHOLD = 3;
    private static final int UNHEALTHY_FAILURE_THRESHOLD = 5;

    public DefaultCapabilityHealthManager(CapabilityRegistry registry) {
        this(registry, new DefaultCapabilityHealthChecker());
    }

    public DefaultCapabilityHealthManager(
            CapabilityRegistry registry,
            CapabilityHealthChecker defaultChecker) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.defaultChecker = Objects.requireNonNull(defaultChecker, "defaultChecker cannot be null");
    }

    public void registerChecker(String capabilityId, CapabilityHealthChecker checker) {
        Objects.requireNonNull(capabilityId, "capabilityId cannot be null");
        Objects.requireNonNull(checker, "checker cannot be null");
        customCheckers.put(capabilityId, checker);
    }

    public Optional<CapabilityProviderStatus> statusOf(String capabilityId, String providerId) {
        if (capabilityId == null || providerId == null) {
            return Optional.empty();
        }
        ConcurrentMap<String, CapabilityProviderStatus> map = statuses.get(capabilityId);
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(providerId));
    }

    public List<CapabilityProviderStatus> statusesOf(String capabilityId) {
        if (capabilityId == null) {
            return List.of();
        }
        ConcurrentMap<String, CapabilityProviderStatus> map = statuses.get(capabilityId);
        if (map == null) {
            return List.of();
        }
        return List.copyOf(map.values());
    }

    public CapabilityProviderStatus check(CapabilityProviderRegistration provider) {
        Objects.requireNonNull(provider, "provider cannot be null");

        CapabilityHealthChecker checker = customCheckers.getOrDefault(
                provider.capabilityId(),
                defaultChecker
        );

        long start = System.currentTimeMillis();
        CapabilityProviderStatus status;
        try {
            status = checker.check(provider);
            recordSuccess(provider.capabilityId(), provider.providerId(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            status = new CapabilityProviderStatus(
                    provider.capabilityId(),
                    provider.providerId(),
                    CapabilityAvailability.AVAILABLE,
                    CapabilityHealth.UNHEALTHY,
                    Instant.now(),
                    System.currentTimeMillis() - start,
                    1.0,
                    "Health check failed: " + e.getMessage(),
                    Map.of("error", e.getClass().getName())
            );
            recordFailure(provider.capabilityId(), provider.providerId(), e);
        }

        updateStatus(status);
        return status;
    }

    public void checkAll() {
        for (CapabilityProviderRegistration provider : registry.allProviderRegistrations()) {
            check(provider);
        }
    }

    public void updateStatus(CapabilityProviderStatus status) {
        Objects.requireNonNull(status, "status cannot be null");
        statuses.computeIfAbsent(status.capabilityId(), k -> new ConcurrentHashMap<>())
                .put(status.providerId(), status);
    }

    public void recordSuccess(String capabilityId, String providerId, long durationMillis) {
        String key = key(capabilityId, providerId);
        failureCounters.remove(key);

        statusOf(capabilityId, providerId).ifPresent(current -> {
            if (current.health() != CapabilityHealth.HEALTHY) {
                updateStatus(new CapabilityProviderStatus(
                        capabilityId,
                        providerId,
                        CapabilityAvailability.AVAILABLE,
                        CapabilityHealth.HEALTHY,
                        Instant.now(),
                        durationMillis,
                        current.load(),
                        "Recovered",
                        current.attributes()
                ));
            }
        });
    }

    public void recordFailure(String capabilityId, String providerId, Throwable error) {
        String key = key(capabilityId, providerId);
        int failures = failureCounters.compute(key, (k, count) -> (count == null ? 0 : count) + 1);

        CapabilityHealth newHealth = failures >= UNHEALTHY_FAILURE_THRESHOLD
                ? CapabilityHealth.UNHEALTHY
                : (failures >= DEGRADED_FAILURE_THRESHOLD
                        ? CapabilityHealth.DEGRADED
                        : CapabilityHealth.HEALTHY);

        CapabilityProviderStatus current = statusOf(capabilityId, providerId).orElse(null);
        Double currentLoad = current != null ? current.load() : 1.0;
        Map<String, Object> attrs = current != null ? current.attributes() : Map.of();

        updateStatus(new CapabilityProviderStatus(
                capabilityId,
                providerId,
                CapabilityAvailability.AVAILABLE,
                newHealth,
                Instant.now(),
                null,
                currentLoad,
                "Failures: " + failures + " (last error: " + (error != null ? error.getMessage() : "unknown") + ")",
                attrs
        ));
    }

    public void markUnavailable(String capabilityId, String providerId, String reason) {
        CapabilityProviderStatus current = statusOf(capabilityId, providerId).orElse(null);
        updateStatus(new CapabilityProviderStatus(
                capabilityId,
                providerId,
                CapabilityAvailability.UNAVAILABLE,
                current != null ? current.health() : CapabilityHealth.UNKNOWN,
                Instant.now(),
                null,
                1.0,
                reason,
                current != null ? current.attributes() : Map.of()
        ));
    }

    public void removeProvider(String capabilityId, String providerId) {
        ConcurrentMap<String, CapabilityProviderStatus> map = statuses.get(capabilityId);
        if (map != null) {
            map.remove(providerId);
        }
        failureCounters.remove(key(capabilityId, providerId));
    }

    private static String key(String capabilityId, String providerId) {
        return capabilityId + ":" + providerId;
    }
}
