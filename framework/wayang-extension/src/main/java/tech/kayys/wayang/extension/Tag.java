package tech.kayys.wayang.core;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Represents a tag or label for categorization.
 */
public record Tag(
    String key,
    String value
) {
    
    public static Tag of(String key) {
        return new Tag(key, null);
    }
    
    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }
    
    @Override
    public String toString() {
        return value != null ? key + "=" + value : key;
    }
}