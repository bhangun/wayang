package tech.kayys.wayang.memory.manager;

import tech.kayys.wayang.memory.MemoryProvider;
import tech.kayys.wayang.memory.MemoryQuery;
import tech.kayys.wayang.memory.MemoryRecord;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class DefaultMemoryManager implements MemoryManager {
    
    private final MemoryProvider memoryProvider;
    
    public DefaultMemoryManager(MemoryProvider memoryProvider) {
        this.memoryProvider = memoryProvider;
    }

    @Override
    public CompletionStage<String> recallContext(String prompt) {
        if (memoryProvider == null || prompt == null || prompt.isBlank()) {
            return CompletableFuture.completedFuture("");
        }
        
        MemoryQuery query = MemoryQuery.builder()
            .query(prompt)
            .limit(5)
            .minRelevance(0.7)
            .build();
        
        return memoryProvider.searchAsync(query).thenApply(records -> {
            if (records == null || records.isEmpty()) {
                return "";
            }
            
            return "Relevant historical context:\n" + records.stream()
                .map(MemoryRecord::value)
                .collect(Collectors.joining("\n---\n"));
        }).exceptionally(ex -> {
            // Log error in a real system, but don't fail the execution just because memory recall failed
            return "";
        });
    }

    @Override
    public CompletionStage<Void> storeInteraction(String requestContent, String responseContent) {
        if (memoryProvider == null || requestContent == null || responseContent == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                String content = "User: " + requestContent + "\nAgent: " + responseContent;
                MemoryRecord record = MemoryRecord.of(UUID.randomUUID().toString(), content, "agent_interaction");
                memoryProvider.save(record);
            } catch (Exception e) {
                // Ignore memory save failures to not crash the main execution loop
            }
        });
    }
}
