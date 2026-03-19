package tech.kayys.wayang.memory.vector;

import tech.kayys.wayang.vector.VectorEntry;
import tech.kayys.wayang.vector.VectorQuery;
import tech.kayys.wayang.vector.VectorStore;
import tech.kayys.wayang.memory.model.Memory;
import tech.kayys.wayang.memory.model.MemoryType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter for integrating VectorStore with Memory systems.
 * Converts between Memory objects and VectorEntry objects.
 */
public class VectorMemoryAdapter {

    private final VectorStore vectorStore;
    private final String indexName;
    private final double similarityThreshold;

    public VectorMemoryAdapter(VectorStore vectorStore, String indexName, double similarityThreshold) {
        this.vectorStore = vectorStore;
        this.indexName = indexName;
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * Store memories as vector entries.
     */
    public void storeMemories(List<Memory> memories) {
        List<VectorEntry> entries = memories.stream()
                .map(this::memoryToVectorEntry)
                .collect(Collectors.toList());
        vectorStore.store(entries);
    }

    /**
     * Search for similar memories.
     */
    public List<Memory> searchSimilarMemories(String query, int topK) {
        VectorQuery vectorQuery = VectorQuery.builder()
                .query(query)
                .topK(topK)
                .threshold(similarityThreshold)
                .build();

        return vectorStore.search(vectorQuery)
                .stream()
                .map(this::vectorEntryToMemory)
                .collect(Collectors.toList());
    }

    /**
     * Search with filters.
     */
    public List<Memory> searchMemories(String query, int topK, Map<String, Object> filters) {
        VectorQuery vectorQuery = VectorQuery.builder()
                .query(query)
                .topK(topK)
                .threshold(similarityThreshold)
                .build();

        return vectorStore.search(vectorQuery, filters)
                .stream()
                .map(this::vectorEntryToMemory)
                .collect(Collectors.toList());
    }

    /**
     * Delete memories by IDs.
     */
    public void deleteMemories(List<String> memoryIds) {
        vectorStore.delete(memoryIds);
    }

    /**
     * Convert Memory to VectorEntry.
     */
    private VectorEntry memoryToVectorEntry(Memory memory) {
        return VectorEntry.builder()
                .id(memory.getId())
                .content(memory.getContent())
                .metadata(Map.ofEntries(
                        Map.entry("type", memory.getType().toString()),
                        Map.entry("timestamp", memory.getTimestamp()),
                        Map.entry("source", memory.getSource()),
                        Map.entry("strength", memory.getStrength())
                ))
                .build();
    }

    /**
     * Convert VectorEntry to Memory.
     */
    private Memory vectorEntryToMemory(VectorEntry entry) {
        Memory.Builder builder = Memory.builder()
                .id(entry.getId())
                .content(entry.getContent())
                .metadata(entry.getMetadata());

        if (entry.getMetadata() != null) {
            Object type = entry.getMetadata().get("type");
            if (type != null) {
                builder.type(MemoryType.valueOf(type.toString()));
            }
            Object timestamp = entry.getMetadata().get("timestamp");
            if (timestamp != null) {
                builder.timestamp(timestamp.toString());
            }
            Object source = entry.getMetadata().get("source");
            if (source != null) {
                builder.source(source.toString());
            }
            Object strength = entry.getMetadata().get("strength");
            if (strength != null) {
                builder.strength(Double.parseDouble(strength.toString()));
            }
        }

        return builder.build();
    }
}
