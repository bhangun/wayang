package tech.kayys.wayang.embedding.executor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.wayang.embedding.EmbeddingRequest;
import tech.kayys.wayang.embedding.EmbeddingService;
import tech.kayys.wayang.embedding.node.EmbeddingNodeTypes;
import tech.kayys.wayang.plugin.executor.AbstractNodeExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Node executor for generating vector embeddings.
 */
@ApplicationScoped
public class EmbeddingNodeExecutor extends AbstractNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddingNodeExecutor.class);

    @Inject
    EmbeddingService embeddingService;

    @Override
    public String getExecutorType() {
        return EmbeddingNodeTypes.EMBEDDING_GENERATE;
    }

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Instant startedAt = Instant.now();
        Map<String, Object> config = task.context();

        // Configuration from node instance
        String model = (String) config.get("model");
        String provider = (String) config.get("provider");
        Boolean normalize = (Boolean) config.get("normalize");

        // Input from task data
        Object input = task.input().get("text");
        if (input == null) {
            return Uni.createFrom().item(failure(task, "Missing 'text' input for embedding generation", startedAt));
        }

        List<String> inputs;
        if (input instanceof List) {
            inputs = (List<String>) input;
        } else {
            inputs = List.of(input.toString());
        }

        LOG.info("Generating embeddings for {} inputs using model: {}, provider: {}", inputs.size(), model, provider);

        EmbeddingRequest request = new EmbeddingRequest(inputs, model, provider, normalize);

        return embeddingService.embed(request)
                .map(response -> {
                    Map<String, Object> output = Map.of(
                            "embeddings", response.embeddings(),
                            "dimension", response.dimension(),
                            "model", response.model(),
                            "provider", response.provider());
                    return success(task, output, startedAt);
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Embedding generation failed: {}", throwable.getMessage());
                    return failure(task, throwable.getMessage(), startedAt);
                });
    }
}
