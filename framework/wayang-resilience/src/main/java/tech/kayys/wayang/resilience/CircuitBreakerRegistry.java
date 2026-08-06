package tech.kayys.wayang.resilience;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.time.*;

/**
 * Circuit Breaker Registry
 */
public class CircuitBreakerRegistry {
    
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final CircuitBreakerConfig defaultConfig;
    
    public CircuitBreakerRegistry() {
        this(CircuitBreakerConfig.defaults());
    }
    
    public CircuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
    
    public CircuitBreaker getOrCreate(String name) {
        return circuitBreakers.computeIfAbsent(name, 
            k -> new DefaultCircuitBreaker(k, defaultConfig));
    }
    
    public CircuitBreaker getOrCreate(String name, CircuitBreakerConfig config) {
        return circuitBreakers.computeIfAbsent(name,
            k -> new DefaultCircuitBreaker(k, config));
    }
    
    public void remove(String name) {
        circuitBreakers.remove(name);
    }
    
    public List<CircuitBreaker> getAll() {
        return new ArrayList<>(circuitBreakers.values());
    }
    
    public void resetAll() {
        for (CircuitBreaker cb : circuitBreakers.values()) {
            cb.reset();
        }
    }
}
