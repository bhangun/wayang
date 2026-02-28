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

import tech.kayys.wayang.eip.dto.RetryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.kayys.wayang.eip.service.RetryService;

import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.retry", maxConcurrentTasks = 50, supportedNodeTypes = {
                "retry", "error-handler" }, version = "1.0.0")
public class RetryExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(RetryExecutor.class);

        @Inject
        RetryService retryService;

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                RetryDto config = objectMapper.convertValue(context, RetryDto.class);

                // In a real scenario, this executor might wrap another action or be part of a
                // larger flow control
                // Since executors are atomic steps, 'retry' as a node usually means "execute
                // the nested logic with retry"
                // But here we are just demonstrating the capability

                LOG.info("Configuring retry policy: {} max attempts", config.maxAttempts());

                return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                                task.runId(),
                                task.nodeId(),
                                task.attempt(),
                                Map.of(
                                                "retryConfig", config,
                                                "attempts", 1, // Placeholder for actual attempts which is managed by
                                                               // engine
                                                "configuredAt", Instant.now().toString()),
                                task.token(), java.time.Duration.ZERO));
        }
}
