package tech.kayys.wayang.rag.memory;

import tech.kayys.wayang.rag.core.RagChunk;
import tech.kayys.wayang.rag.core.RagQuery;
import tech.kayys.wayang.rag.core.RagResult;
import tech.kayys.wayang.rag.core.RetrievalExecutor;
import tech.kayys.wayang.memory.vector.VectorMemoryAdapter;
import tech.kayys.wayang.memory.model.Memory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Integrates RAG retrieval with Memory systems.
 * Combines document retrieval from vector stores with memory retrieval.
 */
public class RagMemoryIntegration {

    private final RetrievalExecutor ragRetriever;
    private final VectorMemoryAdapter vectorMemory;
    private final double memoryWeight;

    public RagMemoryIntegration(RetrievalExecutor ragRetriever, 
                                 VectorMemoryAdapter vectorMemory,
                                 double memoryWeight) {
        this.ragRetriever = ragRetriever;
        this.vectorMemory = vectorMemory;
        this.memoryWeight = memoryWeight;
    }

    /**
     * Retrieve from both RAG documents and memory.
     */
    public RagResult retrieveWithMemory(String query, int ragTopK, int memoryTopK) {
        // Retrieve from RAG
        RagQuery ragQuery = RagQuery.builder()
                .query(query)
                .topK(ragTopK)
                .build();
        RagResult ragResult = ragRetriever.retrieve(ragQuery);

        // Retrieve from memory
        List<Memory> memories = vectorMemory.searchSimilarMemories(query, memoryTopK);

        // Convert memories to RAG chunks
        List<RagChunk> memoryChunks = memories.stream()
                .map(this::memoryToRagChunk)
                .collect(Collectors.toList());

        // Combine results
        List<RagChunk> combined = Stream.concat(
                        ragResult.getChunks().stream(),
                        memoryChunks.stream())
                .collect(Collectors.toList());

        return new RagResult(query, combined, ragResult.getMetadata());
    }

    /**
     * Retrieve with weighted balance between memory and documents.
     */
    public RagResult retrieveWeighted(String query, int totalTopK) {
        int ragTopK = (int) (totalTopK * (1.0 - memoryWeight));
        int memoryTopK = totalTopK - ragTopK;

        return retrieveWithMemory(query, ragTopK, memoryTopK);
    }

    /**
     * Convert Memory to RagChunk.
     */
    private RagChunk memoryToRagChunk(Memory memory) {
        return RagChunk.builder()
                .content(memory.getContent())
                .metadata(memory.getMetadata())
                .source("memory:" + memory.getType().toString().toLowerCase())
                .score(memory.getStrength())
                .build();
    }
}
