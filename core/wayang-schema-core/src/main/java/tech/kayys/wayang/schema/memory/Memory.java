package tech.kayys.wayang.schema.memory;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.kayys.wayang.schema.common.Metadata;
import java.time.Duration;
import java.util.Map;

/**
 * Represents a memory system for storing and retrieving information.
 */
public class Memory {
    @JsonProperty("metadata")
    private Metadata metadata;

    @JsonProperty("type")
    private String type;

    @JsonProperty("capacity")
    private int capacity;

    @JsonProperty("ttl")
    private Duration ttl;

    @JsonProperty("evictionPolicy")
    private String evictionPolicy;

    @JsonProperty("configuration")
    private Map<String, Object> configuration;

    public Memory() {
        // Default constructor for JSON deserialization
    }

    public Memory(Metadata metadata, String type, int capacity, Duration ttl, 
                 String evictionPolicy, Map<String, Object> configuration) {
        this.metadata = metadata;
        this.type = type;
        this.capacity = capacity;
        this.ttl = ttl;
        this.evictionPolicy = evictionPolicy;
        this.configuration = configuration;
    }

    // Getters
    public Metadata getMetadata() {
        return metadata;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    public Duration getTtl() {
        return ttl;
    }

    public String getEvictionPolicy() {
        return evictionPolicy;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    // Setters
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public void setEvictionPolicy(String evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}