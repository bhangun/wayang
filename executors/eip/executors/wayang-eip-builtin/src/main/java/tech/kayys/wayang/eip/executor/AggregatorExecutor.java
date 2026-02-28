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

import tech.kayys.wayang.eip.dto.AggregatorDto;
import tech.kayys.wayang.eip.service.AggregatorStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.aggregator", maxConcurrentTasks = 50, supportedNodeTypes = {
                "aggregator", "joiner" }, version = "1.0.0")
public class AggregatorExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(AggregatorExecutor.class);

        @Inject
        AggregatorStore aggregatorStore;

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                AggregatorDto config = objectMapper.convertValue(context, AggregatorDto.class);

                String correlationId = (String) context.get(config.correlationKey());
                Object message = context.get("message");

                if (correlationId == null) {
                        return Uni.createFrom().failure(
                                        new IllegalArgumentException(
                                                        "Missing correlation key: " + config.correlationKey()));
                }

                return aggregatorStore.add(correlationId, message, config)
                                .flatMap(aggregation -> {
                                        if (aggregation.isComplete()) {
                                                return aggregatorStore.remove(correlationId)
                                                                .map(messages -> {
                                                                        LOG.info("Aggregation complete for {}",
                                                                                        correlationId);
                                                                        return SimpleNodeExecutionResult.success(
                                                                                        task.runId(),
                                                                                        task.nodeId(),
                                                                                        task.attempt(),
                                                                                        Map.of(
                                                                                                        "aggregatedMessages",
                                                                                                        messages,
                                                                                                        "count",
                                                                                                        messages.size(),
                                                                                                        "messageCount",
                                                                                                        messages.size(),
                                                                                                        "completedAt",
                                                                                                        Instant.now().toString()),
                                                                                        task.token(),
                                                                                        java.time.Duration.ZERO);
                                                                });
                                        } else {
                                                LOG.debug("Aggregation pending for {}", correlationId);
                                                return Uni.createFrom().item(new SimpleNodeExecutionResult(
                                                                task.runId(),
                                                                task.nodeId(),
                                                                task.attempt(),
                                                                tech.kayys.gamelan.engine.node.NodeExecutionStatus.PENDING,
                                                                Map.of(),
                                                                null,
                                                                task.token(),
                                                                java.time.Instant.now(),
                                                                java.time.Duration.ZERO,
                                                                null,
                                                                null,
                                                                Map.of()));
                                        }
                                });
        }
}
