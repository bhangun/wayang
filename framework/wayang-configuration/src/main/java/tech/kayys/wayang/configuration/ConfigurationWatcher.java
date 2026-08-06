package tech.kayys.wayang.configuration;

import java.util.*;
import java.util.concurrent.*;
import java.time.Instant;
import java.nio.file.*;

/**
 * Configuration Watcher - monitors configuration changes
 */
public class ConfigurationWatcher {
    
    private final ConfigurationRegistry registry;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<ConfigId, Long> lastCheckTimes = new ConcurrentHashMap<>();
    private final List<ConfigurationChangeListener> listeners = new CopyOnWriteArrayList<>();
    private boolean running = false;
    
    public ConfigurationWatcher(ConfigurationRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Start watching for changes
     */
    public void start() {
        if (running) return;
        running = true;
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkForChanges();
            } catch (Exception e) {
                // Log error
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Stop watching
     */
    public void stop() {
        running = false;
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
    
    /**
     * Add listener
     */
    public void addListener(ConfigurationChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove listener
     */
    public void removeListener(ConfigurationChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void checkForChanges() throws Exception {
        for (ConfigurationResource config : registry.list()) {
            if (config.source() == ConfigSource.FILE) {
                checkFileChange(config);
            }
            // Add other source checks
        }
    }
    
    private void checkFileChange(ConfigurationResource config) throws Exception {
        Path path = Paths.get(config.path());
        if (!Files.exists(path)) {
            return;
        }
        
        long lastModified = Files.getLastModifiedTime(path).toMillis();
        Long lastCheck = lastCheckTimes.get(config.id());
        
        if (lastCheck == null || lastModified > lastCheck) {
            lastCheckTimes.put(config.id(), lastModified);
            
            // Reload configuration
            ConfigurationResource newConfig = config.reload();
            registry.register(newConfig);
            
            // Notify listeners
            ConfigurationChangeEvent event = new ConfigurationChangeEvent(
                config.id(),
                config.path(),
                config.allValues(),
                newConfig.allValues(),
                Instant.now()
            );
            for (ConfigurationChangeListener listener : listeners) {
                listener.onConfigurationChanged(event);
            }
        }
    }
}