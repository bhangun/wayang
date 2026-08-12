package tech.kayys.wayang.memory.manager;

import java.util.concurrent.CompletionStage;

/**
 * High-level manager for agent memory.
 * Interacts with the underlying MemoryProvider to recall and store interactions.
 */
public interface MemoryManager {
    
    /**
     * Recalls relevant context based on the current prompt/request.
     * @param prompt the current request content
     * @return A semantic string of recalled memory
     */
    CompletionStage<String> recallContext(String prompt);
    
    /**
     * Stores the interaction (request and response) in long-term memory.
     * @param requestContent the initial request
     * @param responseContent the final response
     */
    CompletionStage<Void> storeInteraction(String requestContent, String responseContent);
}
