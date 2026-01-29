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

import tech.kayys.wayang.integration.core.config.ChannelConfig;
import tech.kayys.wayang.integration.core.service.ChannelRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import tech.kayys.gamelan.engine.protocol.CommunicationType;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.channel", communicationType = CommunicationType.KAFKA, maxConcurrentTasks = 100, supportedNodeTypes = {
                "channel", "message-channel" }, version = "1.0.0")
public class ChannelExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(ChannelExecutor.class);

        @Inject
        ChannelRegistry channelRegistry;

        @Inject
        AuditService auditService;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                ChannelConfig config = ChannelConfig.fromContext(context);
                Object message = context.get("message");
                String operation = (String) context.getOrDefault("operation", "send");

                LOG.info("Channel operation: {}, channel: {}", operation, config.channelName());

                return switch (operation) {
                        case "send" -> sendToChannel(config, message, task);
                        case "receive" -> receiveFromChannel(config, task);
                        case "peek" -> peekChannel(config, task);
                        case "size" -> getChannelSize(config, task);
                        default -> Uni.createFrom().failure(
                                        new IllegalArgumentException("Unknown operation: " + operation));
                };
        }

        private Uni<NodeExecutionResult> sendToChannel(ChannelConfig config, Object message, NodeExecutionTask task) {
                return channelRegistry.getChannel(config.channelName())
                                .send(message)
                                .map(messageId -> {
                                        auditService.recordEvent(task, "CHANNEL_SEND",
                                                        Map.of("channel", config.channelName(), "messageId",
                                                                        messageId));

                                        return SimpleNodeExecutionResult.success(
                                                        task.runId(),
                                                        task.nodeId(),
                                                        task.attempt(),
                                                        Map.of(
                                                                        "messageId", messageId,
                                                                        "channel", config.channelName(),
                                                                        "operation", "send",
                                                                        "sentAt", Instant.now().toString()),
                                                        task.token(), Duration.ZERO);
                                });
        }

        private Uni<NodeExecutionResult> receiveFromChannel(ChannelConfig config, NodeExecutionTask task) {
                return channelRegistry.getChannel(config.channelName())
                                .receive()
                                .map(message -> SimpleNodeExecutionResult.success(
                                                task.runId(),
                                                task.nodeId(),
                                                task.attempt(),
                                                Map.of(
                                                                "message", message != null ? message : "NO_MESSAGE",
                                                                "channel", config.channelName(),
                                                                "operation", "receive",
                                                                "receivedAt", Instant.now().toString()),
                                                task.token(), Duration.ZERO));
        }

        private Uni<NodeExecutionResult> peekChannel(ChannelConfig config, NodeExecutionTask task) {
                return channelRegistry.getChannel(config.channelName())
                                .peek()
                                .map(message -> SimpleNodeExecutionResult.success(
                                                task.runId(),
                                                task.nodeId(),
                                                task.attempt(),
                                                Map.of(
                                                                "message", message != null ? message : "NO_MESSAGE",
                                                                "channel", config.channelName(),
                                                                "operation", "peek"),
                                                task.token(), Duration.ZERO));
        }

        private Uni<NodeExecutionResult> getChannelSize(ChannelConfig config, NodeExecutionTask task) {
                return channelRegistry.getChannel(config.channelName())
                                .size()
                                .map(size -> SimpleNodeExecutionResult.success(
                                                task.runId(),
                                                task.nodeId(),
                                                task.attempt(),
                                                Map.of(
                                                                "size", size,
                                                                "channel", config.channelName(),
                                                                "operation", "size"),
                                                task.token(), Duration.ZERO));
        }
}
