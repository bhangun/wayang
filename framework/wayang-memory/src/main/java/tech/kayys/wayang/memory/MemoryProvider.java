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
     * Name or identifier of this memory provider/tier (e.g. short-term, working, episodic, long-term).
     */
    default String name() {
        return getClass().getSimpleName();
    }
    
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

    /**
     * Export all memory records for a given agent/namespace as a portable snapshot.
     * Implementations should return all records regardless of TTL state, ordered oldest-first.
     */
    default List<MemoryRecord> exportAll(String agentId) throws Exception {
        return search(MemoryQuery.builder()
                .filter("agentId", agentId)
                .limit(Integer.MAX_VALUE)
                .build());
    }

    /**
     * Import (restore) memory records from a snapshot.
     * Implementations should upsert: update existing records with same key,
     * and insert records that do not yet exist.
     *
     * @param records the records to restore
     * @param overwrite if true, overwrite records with the same key; if false, skip existing
     * @return count of records actually written
     */
    default int importAll(List<MemoryRecord> records, boolean overwrite) throws Exception {
        int count = 0;
        for (MemoryRecord record : records) {
            if (!overwrite) {
                Optional<MemoryRecord> existing = get(record.key());
                if (existing.isPresent()) continue;
            }
            save(record);
            count++;
        }
        return count;
    }
}

