package tech.kayys.wayang.integration.core.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.integration.core.service.AuditService;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.wayang.integration.core.config.DeadLetterChannelConfig;
import tech.kayys.wayang.integration.core.model.DeadLetterMessage;
import tech.kayys.wayang.integration.core.service.DeadLetterStore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tech.kayys.gamelan.engine.protocol.CommunicationType;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.dead-letter-channel", communicationType = CommunicationType.KAFKA, maxConcurrentTasks = 50, supportedNodeTypes = {
        "dead-letter-channel", "dlc" }, version = "1.0.0")
public class DeadLetterChannelExecutor extends AbstractWorkflowExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(DeadLetterChannelExecutor.class);

    @Inject
    DeadLetterStore deadLetterStore;

    @Inject
    AuditService auditService;

    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        Map<String, Object> context = task.context();
        DeadLetterChannelConfig config = DeadLetterChannelConfig.fromContext(context);
        Object failedMessage = context.get("message");
        Object error = context.get("error");

        LOG.warn("Sending to dead letter channel: {}", config.channelName());

        DeadLetterMessage dlm = new DeadLetterMessage(
                UUID.randomUUID().toString(),
                failedMessage,
                error,
                task.runId().value(),
                task.nodeId().value(),
                Instant.now(),
                Instant.now().plus(config.retention()),
                extractErrorDetails(error));

        return deadLetterStore.store(config.channelName(), dlm)
                .map(stored -> {
                    if (config.logErrors()) {
                        LOG.info("Dead letter stored: {}, error: {}", dlm.id(), error);
                    }

                    if (config.notifyAdmin()) {
                        notifyAdmin(dlm, config);
                    }

                    auditService.recordEvent(task, "DEAD_LETTER_STORED",
                            Map.of("dlId", dlm.id(), "channel", config.channelName()));

                    return SimpleNodeExecutionResult.success(
                            task.runId(),
                            task.nodeId(),
                            task.attempt(),
                            Map.of(
                                    "deadLetterId", dlm.id(),
                                    "channel", config.channelName(),
                                    "storedAt", dlm.timestamp().toString(),
                                    "sentToDLC", true,
                                    "originalMessage", failedMessage,
                                    "canRetry", true),
                            task.token(), java.time.Duration.ZERO);
                });
    }

    private Map<String, Object> extractErrorDetails(Object error) {
        Map<String, Object> details = new HashMap<>();

        if (error instanceof Throwable throwable) {
            details.put("errorType", throwable.getClass().getName());
            details.put("message", throwable.getMessage());
            details.put("stackTrace", getStackTrace(throwable));
        } else if (error instanceof Map) {
            details.putAll((Map<String, Object>) error);
        } else if (error != null) {
            details.put("error", error.toString());
        }

        return details;
    }

    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 1000)
                break; // Limit stack trace size
        }
        return sb.toString();
    }

    private void notifyAdmin(DeadLetterMessage dlm, DeadLetterChannelConfig config) {
        // In production, send email/slack/pagerduty notification
        LOG.warn("ADMIN NOTIFICATION: Dead letter message {} in channel {}",
                dlm.id(), config.channelName());
    }
}
