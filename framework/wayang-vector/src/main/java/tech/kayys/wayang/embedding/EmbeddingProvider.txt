package tech.kayys.wayang.embedding;
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
 * Embedding Provider - generates embeddings.
 */
public interface EmbeddingProvider extends Extension {
    
    /**
     * Generate embedding for text
     */
    EmbeddingVector embed(String text) throws Exception;
    
    /**
     * Generate embeddings for multiple texts
     */
    default List<EmbeddingVector> embed(List<String> texts) throws Exception {
        List<EmbeddingVector> results = new ArrayList<>();
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }
    
    /**
     * Generate embedding asynchronously
     */
    default CompletableFuture<EmbeddingVector> embedAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return embed(text);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get embedding model info
     */
    default EmbeddingModelInfo getModelInfo() {
        return new EmbeddingModelInfo("default", 768, "unknown");
    }
}