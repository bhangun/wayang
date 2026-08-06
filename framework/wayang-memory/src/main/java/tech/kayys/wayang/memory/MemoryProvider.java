package tech.kayys.wayang.memory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.*;
import java.util.concurrent.CompletableFuture;


import tech.kayys.wayang.extension.Extension;
/**
 * Memory Provider - manages memory storage and retrieval.
 */
public interface MemoryProvider extends Extension {
    
    /**
     * Save a memory record
     */
    void save(MemoryRecord record) throws Exception;
    
    /**
     * Save multiple memory records
     */
    void save(List<MemoryRecord> records) throws Exception;
    
    /**
     * Search memory
     */
    List<MemoryRecord> search(MemoryQuery query) throws Exception;
    
    /**
     * Get a memory record by key
     */
    Optional<MemoryRecord> get(String key) throws Exception;
    
    /**
     * Delete a memory record
     */
    void delete(String key) throws Exception;
    
    /**
     * Clear all memory
     */
    void clear() throws Exception;
    
    /**
     * Search asynchronously
     */
    default CompletableFuture<List<MemoryRecord>> searchAsync(MemoryQuery query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return search(query);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get memory statistics
     */
    default MemoryStats getStats() throws Exception {
        return new MemoryStats(0, 0, Map.of(), Map.of());
    }
}
