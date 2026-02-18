package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.wayang.eip.config.EnricherConfig;
import tech.kayys.wayang.eip.config.EnrichmentSource;
import tech.kayys.wayang.eip.service.EnrichmentService;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.enricher", maxConcurrentTasks = 50, supportedNodeTypes = {
        "enricher", "content-enricher" }, version = "1.0.0")
public class EnricherExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(EnricherExecutor.class);

    @Inject
    EnrichmentService enrichmentService;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        EnricherConfig config = EnricherConfig.fromContext(context);
        Object message = context.get("message");

        // Start with empty enrichment map
        Uni<Map<String, Object>> enrichmentUni = Uni.createFrom().item(new HashMap<>());

        // Chain enrichments from all sources
        for (EnrichmentSource source : config.sources()) {
            enrichmentUni = enrichmentUni
                    .flatMap(currentEnrichment -> enrichmentService.enrich(source, message, context)
                            .map(newEnrichment -> {
                                currentEnrichment.putAll(newEnrichment);
                                return currentEnrichment;
                            }));
        }

        return enrichmentUni.map(enrichment -> {
            LOG.info("Enriched message with {} fields", enrichment.size());

            // Merge strategy
            Object enrichedMessage;
            if ("merge".equals(config.mergeStrategy()) && message instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> merged = new HashMap<>((Map<String, Object>) message);
                merged.putAll(enrichment);
                enrichedMessage = merged;
            } else if ("wrap".equals(config.mergeStrategy())) {
                enrichedMessage = Map.of(
                        "original", message,
                        "enrichment", enrichment);
            } else {
                // Default: add enrichment to message if map, or replace if not
                if (message instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> merged = new HashMap<>((Map<String, Object>) message);
                    merged.putAll(enrichment);
                    enrichedMessage = merged;
                } else {
                    enrichedMessage = message;
                }
            }

            return SimpleNodeExecutionResult.success(
                    task.runId(),
                    task.nodeId(),
                    task.attempt(),
                    Map.of(
                            "message", enrichedMessage,
                            "enrichment", enrichment,
                            "enrichedAt", Instant.now().toString()),
                    task.token(), Duration.ZERO);
        });
    }
}
