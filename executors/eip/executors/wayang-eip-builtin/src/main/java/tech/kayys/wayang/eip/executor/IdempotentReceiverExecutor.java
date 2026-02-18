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

import tech.kayys.wayang.eip.config.IdempotentReceiverConfig;
import tech.kayys.wayang.eip.service.IdempotencyStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.idempotent", maxConcurrentTasks = 100, supportedNodeTypes = {
        "idempotent-consumer", "deduplicator" }, version = "1.0.0")
public class IdempotentReceiverExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(IdempotentReceiverExecutor.class);

    @Inject
    IdempotencyStore idempotencyStore;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        IdempotentReceiverConfig config = IdempotentReceiverConfig.fromContext(context);

        // Extract key
        String key = extractKey(context, config.idempotencyKeyField());
        if (key == null) {
            return Uni.createFrom().failure(
                    new IllegalArgumentException("Missing idempotency key field: " + config.idempotencyKeyField()));
        }

        return idempotencyStore.checkAndRecord(key, config.windowDuration())
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        LOG.info("Duplicate message detected: {}", key);
                        if ("skip".equals(config.action())) {
                            return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                                    task.runId(),
                                    task.nodeId(),
                                    task.attempt(),
                                    Map.of("duplicate", true, "action", "skipped"),
                                    task.token(), Duration.ZERO));
                        } else {
                            return Uni.createFrom().failure(new RuntimeException("Duplicate message detected")); // Or
                                                                                                                 // handle
                                                                                                                 // error
                        }
                    }

                    LOG.debug("Processing new message: {}", key);
                    return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                            task.runId(),
                            task.nodeId(),
                            task.attempt(),
                            Map.of("duplicate", false, "processedAt", Instant.now().toString()),
                            task.token(), Duration.ZERO));
                });
    }

    private String extractKey(Map<String, Object> context, String field) {
        Object val = context.get(field);
        if (val != null)
            return val.toString();

        Object msg = context.get("message");
        if (msg instanceof Map) {
            Object v = ((Map) msg).get(field);
            if (v != null)
                return v.toString();
        }
        return null; // UUID.randomUUID().toString(); // Don't generate unexpected keys
    }
}
