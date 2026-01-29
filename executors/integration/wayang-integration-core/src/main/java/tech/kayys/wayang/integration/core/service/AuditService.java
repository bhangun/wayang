package tech.kayys.wayang.integration.core.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;

import io.smallrye.mutiny.Uni;
import java.util.Map;

/**
 * Service to record execution events for EIP components
 */
@ApplicationScoped
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    public Uni<Void> recordEvent(NodeExecutionTask task, String eventType, Map<String, Object> metadata) {
        LOG.info("Audit Event: {} - Node: {} - Type: {} - Metadata: {}",
                task.runId(), task.nodeId(), eventType, metadata);
        return Uni.createFrom().voidItem();
    }
}
