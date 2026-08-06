package tech.kayys.wayang.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultCacheProvider implements CacheProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long defaultTtlSeconds = 3600;
    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;
    
    public DefaultCacheProvider() {
        this.id = Id.random().asString();
        this.name = "default-cache-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Cache Provider")
            .version(version)
            .label("type", "cache")
            .now()
            .build();
        
        // Start cleanup thread
        startCleanupThread();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("cache"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) throws Exception {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            hits++;
            return (T) entry.value();
        }
        misses++;
        if (entry != null) {
            cache.remove(key);
            evictions++;
        }
        return null;
    }
    
    @Override
    public void put(String key, Object value) throws Exception {
        put(key, value, defaultTtlSeconds);
    }
    
    @Override
    public void put(String key, Object value, long ttlSeconds) throws Exception {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlSeconds * 1000));
    }
    
    @Override
    public void remove(String key) throws Exception {
        cache.remove(key);
    }
    
    @Override
    public boolean exists(String key) throws Exception {
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return entry != null;
    }
    
    @Override
    public void clear() throws Exception {
        cache.clear();
    }
    
    @Override
    public CacheStats getStats() throws Exception {
        return new CacheStats(hits, misses, evictions, cache.size());
    }
    
    private void startCleanupThread() {
        Thread cleanup = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Run every minute
                    cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanup.setDaemon(true);
        cleanup.start();
    }
    
    private record CacheEntry(Object value, long expiryTime) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}