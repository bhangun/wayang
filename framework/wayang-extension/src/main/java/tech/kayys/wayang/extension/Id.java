package tech.kayys.wayang.extension;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



import java.util.UUID;

/**
 * Base identifier type for all resources in Wayang.
 * Uses Java records for immutability and value semantics.
 */
public record Id(UUID value) {
    
    public static Id random() {
        return new Id(UUID.randomUUID());
    }
    
    public static Id fromString(String value) {
        return new Id(UUID.fromString(value));
    }
    
    public static Id fromUUID(UUID value) {
        return new Id(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
    
    public String asString() {
        return value.toString();
    }
}