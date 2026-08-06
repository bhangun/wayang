package tech.kayys.wayang.health;


import java.util.*;
import java.util.concurrent.*;
import java.time.*;

/**
 * Health Check Registry Implementation
 */
public class DefaultHealthRegistry implements HealthRegistry {
    
    private final Map<String, HealthCheck> checks = new ConcurrentHashMap<>();
    private final Map<String, HealthResult> cachedResults = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final long cacheDurationMs = 5000; // 5 seconds
    
    public DefaultHealthRegistry() {
        // Schedule periodic health checks
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAllAsync();
            } catch (Exception e) {
                // Ignore
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void register(HealthCheck check) {
        checks.put(check.name(), check);
    }
    
    @Override
    public void unregister(String name) {
        checks.remove(name);
        cachedResults.remove(name);
    }
    
    @Override
    public HealthResult check(String name) throws Exception {
        HealthCheck check = checks.get(name);
        if (check == null) {
            return HealthResult.unknown("Health check not found: " + name);
        }
        
        try {
            HealthResult result = check.check();
            cachedResults.put(name, result);
            return result;
        } catch (Exception e) {
            HealthResult result = HealthResult.unhealthy(e.getMessage());
            cachedResults.put(name, result);
            return result;
        }
    }
    
    @Override
    public Map<String, HealthResult> checkAll() throws Exception {
        Map<String, HealthResult> results = new LinkedHashMap<>();
        for (String name : checks.keySet()) {
            results.put(name, check(name));
        }
        return results;
    }
    
    @Override
    public Map<String, HealthResult> checkAllWithTimeout(long timeoutMs) throws Exception {
        Map<String, HealthResult> results = new LinkedHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(checks.size(), 10));
        List<Future<Map.Entry<String, HealthResult>>> futures = new ArrayList<>();
        
        for (String name : checks.keySet()) {
            futures.add(executor.submit(() -> {
                try {
                    return Map.entry(name, check(name));
                } catch (Exception e) {
                    return Map.entry(name, HealthResult.unhealthy(e.getMessage()));
                }
            }));
        }
        
        for (Future<Map.Entry<String, HealthResult>> future : futures) {
            try {
                Map.Entry<String, HealthResult> entry = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                results.put(entry.getKey(), entry.getValue());
            } catch (TimeoutException e) {
                // Skip
            } catch (Exception e) {
                // Skip
            }
        }
        
        executor.shutdownNow();
        return results;
    }
    
    @Override
    public boolean isHealthy() {
        try {
            Map<String, HealthResult> results = checkAllWithTimeout(1000);
            return results.values().stream().allMatch(r -> r.status() == HealthStatus.HEALTHY);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isReady() {
        // Check if all readiness checks pass
        for (HealthCheck check : checks.values()) {
            if (check instanceof ReadinessCheck) {
                try {
                    HealthResult result = check.check();
                    if (result.status() != HealthStatus.HEALTHY) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void checkAllAsync() {
        for (String name : checks.keySet()) {
            executorService.submit(() -> {
                try {
                    check(name);
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}