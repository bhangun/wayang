package tech.kayys.wayang.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultMemoryProvider implements MemoryProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, MemoryRecord> memory = new ConcurrentHashMap<>();
    
    public DefaultMemoryProvider() {
        this.id = Id.random().asString();
        this.name = "default-memory-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Memory Provider")
            .version(version)
            .label("type", "memory")
            .now()
            .build();
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
    public ResourceType type() { return new ResourceType.Custom("memory"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public void save(MemoryRecord record) throws Exception {
        memory.put(record.key(), record);
    }
    
    @Override
    public void save(List<MemoryRecord> records) throws Exception {
        for (MemoryRecord record : records) {
            save(record);
        }
    }
    
    @Override
    public List<MemoryRecord> search(MemoryQuery query) throws Exception {
        return memory.values().stream()
            .filter(record -> {
                if (query.type() != null && !query.type().equals(record.type())) {
                    return false;
                }
                if (query.query() != null && !record.value().toLowerCase()
                        .contains(query.query().toLowerCase())) {
                    return false;
                }
                return true;
            })
            .sorted((a, b) -> Double.compare(b.relevance(), a.relevance()))
            .limit(query.limit())
            .toList();
    }
    
    @Override
    public Optional<MemoryRecord> get(String key) throws Exception {
        return Optional.ofNullable(memory.get(key));
    }
    
    @Override
    public void delete(String key) throws Exception {
        memory.remove(key);
    }
    
    @Override
    public void clear() throws Exception {
        memory.clear();
    }
    
    @Override
    public MemoryStats getStats() throws Exception {
        Map<String, Long> typeCounts = memory.values().stream()
            .collect(Collectors.groupingBy(MemoryRecord::type, Collectors.counting()));
        
        Map<String, Long> userCounts = memory.values().stream()
            .filter(r -> r.userId() != null)
            .collect(Collectors.groupingBy(MemoryRecord::userId, Collectors.counting()));
        
        long totalSize = memory.values().stream()
            .mapToLong(r -> r.value().length() * 2L)
            .sum();
        
        return new MemoryStats(memory.size(), totalSize, typeCounts, userCounts);
    }
}