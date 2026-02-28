package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.eip.service.AuditService;
import tech.kayys.wayang.eip.client.EndpointClientRegistry;

import tech.kayys.wayang.eip.client.EndpointClient;
import tech.kayys.wayang.eip.dto.EndpointDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.gamelan.engine.protocol.CommunicationType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * ============================================================================
 * REAL IMPLEMENTATION - ENDPOINT EXECUTOR
 * ============================================================================
 */

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.endpoint", communicationType = CommunicationType.GRPC, maxConcurrentTasks = 50, supportedNodeTypes = {
                "endpoint", "http-endpoint", "kafka-endpoint" }, version = "1.0.0")
public class EndpointExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(EndpointExecutor.class);

        @Inject
        EndpointClientRegistry clientRegistry;

        @Inject
        AuditService auditService;

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                EndpointDto config = objectMapper.convertValue(context, EndpointDto.class);
                Object payload = context.get("payload");

                LOG.info("Sending to endpoint: {}, protocol: {}", config.uri(), config.protocol());

                return auditService.recordEvent(task, "ENDPOINT_SEND_START", Map.of("uri", config.uri()))
                                .flatMap(v -> sendToEndpoint(config, payload, task))
                                .flatMap(response -> auditService.recordEvent(task, "ENDPOINT_SEND_SUCCESS",
                                                Map.of("uri", config.uri(), "responseSize",
                                                                response.toString().length()))
                                                .replaceWith(response))
                                .map(response -> {
                                        int status = 200;
                                        if (response instanceof Map<?, ?> map && map.containsKey("statusCode")) {
                                                Object code = map.get("statusCode");
                                                if (code instanceof Integer) {
                                                        status = (Integer) code;
                                                }
                                        }
                                        return SimpleNodeExecutionResult.success(
                                                        task.runId(),
                                                        task.nodeId(),
                                                        task.attempt(),
                                                        Map.of(
                                                                        "response", response,
                                                                        "status", status,
                                                                        "endpoint", config.uri(),
                                                                        "sentAt", Instant.now().toString()),
                                                        task.token(), java.time.Duration.ZERO);
                                })
                                .onFailure().invoke(error -> {
                                        LOG.error("Endpoint send failed: {}", config.uri(), error);
                                });
        }

        private Uni<Object> sendToEndpoint(EndpointDto config, Object payload, NodeExecutionTask task) {
                EndpointClient client = clientRegistry.getClient(config.protocol());
                return client.send(config, payload)
                                .ifNoItem().after(Duration.ofMillis(config.timeoutMs()))
                                .failWith(() -> new TimeoutException("Endpoint timeout: " + config.uri()));
        }
}
