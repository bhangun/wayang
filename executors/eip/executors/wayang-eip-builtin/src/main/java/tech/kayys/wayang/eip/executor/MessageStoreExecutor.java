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

import tech.kayys.wayang.eip.dto.MessageStoreDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.kayys.wayang.eip.service.InMemoryMessageStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.message-store", maxConcurrentTasks = 50, supportedNodeTypes = {
                "message-store", "claim-check" }, version = "1.0.0")
public class MessageStoreExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(MessageStoreExecutor.class);

        @Inject
        InMemoryMessageStore messageStore; // Using concrete implementation for now, should use interface and factory

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                MessageStoreDto config = objectMapper.convertValue(context, MessageStoreDto.class);
                Object message = context.get("message");
                String operation = (String) context.getOrDefault("operation", "store");

                if ("store".equals(operation)) {
                        return messageStore.store(message, Duration.ofDays(config.retentionDays()))
                                        .map(id -> {
                                                LOG.info("Stored message with ID: {}", id);
                                                return SimpleNodeExecutionResult.success(
                                                                task.runId(),
                                                                task.nodeId(),
                                                                task.attempt(),
                                                                Map.of(
                                                                                "messageId", id,
                                                                                "storedMessageId", id, // Keep for
                                                                                                       // backward
                                                                                                       // compatibility
                                                                                "storedAt", Instant.now().toString()),
                                                                task.token(), Duration.ZERO);
                                        });
                } else if ("retrieve".equals(operation)) {
                        String messageId = (String) context.getOrDefault("messageId", context.get("storedMessageId"));
                        return messageStore.retrieve(messageId)
                                        .map(retrieved -> SimpleNodeExecutionResult.success(
                                                        task.runId(),
                                                        task.nodeId(),
                                                        task.attempt(),
                                                        Map.of(
                                                                        "message", retrieved,
                                                                        "retrievedAt", Instant.now().toString()),
                                                        task.token(), Duration.ZERO));
                } else {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("Unknown operation: " + operation));
                }
        }
}
