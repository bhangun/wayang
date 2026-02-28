package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.eip.service.AuditService;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.wayang.eip.dto.CorrelationIdDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.kayys.wayang.eip.service.CorrelationService;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.correlation-id", maxConcurrentTasks = 100, supportedNodeTypes = {
        "correlation-id", "correlation-tracker" }, version = "1.0.0")
public class CorrelationIdExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CorrelationIdExecutor.class);

    @Inject
    CorrelationService correlationService;

    @Inject
    AuditService auditService;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        CorrelationIdDto config = objectMapper.convertValue(context, CorrelationIdDto.class);
        Object message = context.get("message");

        return generateOrExtractCorrelationId(config, message, context)
                .flatMap(correlationId -> {
                    LOG.debug("Correlation ID: {}", correlationId);

                    // Track correlation
                    correlationService.track(correlationId, task.runId().value(), task.nodeId().value());

                    auditService.recordEvent(task, "CORRELATION_ID_SET",
                            Map.of("correlationId", correlationId, "strategy", config.strategy()));

                    // Enrich context with correlation ID
                    Map<String, Object> enrichedContext = new HashMap<>(context);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> headers = new HashMap<>(
                            (Map<String, Object>) context.getOrDefault("headers", Map.of()));
                    headers.put(config.headerName(), correlationId);
                    enrichedContext.put("headers", headers);

                    // Add to message if it's a map
                    Object enrichedMessage = message;
                    if (message instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> msgMap = new HashMap<>((Map<String, Object>) message);
                        msgMap.put("_correlationId", correlationId);
                        enrichedMessage = msgMap;
                    }

                    return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                            task.runId(),
                            task.nodeId(),
                            task.attempt(),
                            Map.of(
                                    "correlationId", correlationId,
                                    "message", enrichedMessage,
                                    "headers", headers,
                                    "trackedAt", Instant.now().toString()),
                            task.token(), Duration.ZERO));
                });
    }

    private Uni<String> generateOrExtractCorrelationId(
            CorrelationIdDto config, Object message, Map<String, Object> context) {

        return switch (config.strategy()) {
            case "generate" -> generateNewId();
            case "extract" -> extractCorrelationId(config, message, context);
            case "propagate" -> propagateCorrelationId(config, context);
            default -> generateNewId();
        };
    }

    private Uni<String> generateNewId() {
        return Uni.createFrom().item(() -> {
            // Generate UUID v4
            return UUID.randomUUID().toString();
        });
    }

    private Uni<String> extractCorrelationId(
            CorrelationIdDto config, Object message, Map<String, Object> context) {

        return Uni.createFrom().item(() -> {
            if (config.extractFrom() != null) {
                // Try to extract from message
                if (message instanceof Map) {
                    Object extracted = ((Map<?, ?>) message).get(config.extractFrom());
                    if (extracted != null) {
                        return extracted.toString();
                    }
                }

                // Try to extract from context
                Object contextValue = context.get(config.extractFrom());
                if (contextValue != null) {
                    return contextValue.toString();
                }
            }

            // Fallback to generation
            return UUID.randomUUID().toString();
        });
    }

    private Uni<String> propagateCorrelationId(
            CorrelationIdDto config, Map<String, Object> context) {

        return Uni.createFrom().item(() -> {
            // Try to get from headers
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) context.get("headers");
            if (headers != null) {
                Object correlationId = headers.get(config.headerName());
                if (correlationId != null) {
                    return correlationId.toString();
                }
            }

            // Try to get from context
            Object contextCorrelation = context.get("correlationId");
            if (contextCorrelation != null) {
                return contextCorrelation.toString();
            }

            // Fallback to generation
            return UUID.randomUUID().toString();
        });
    }
}
