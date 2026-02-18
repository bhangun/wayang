package tech.kayys.wayang.agent.repository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.dto.AuditLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryAuditLogRepository implements AuditLogRepository {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryAuditLogRepository.class);
    private final List<AuditLogEntry> logs = new ArrayList<>();

    @Override
    public Uni<Void> save(AuditLogEntry entry) {
        logs.add(entry);
        LOG.debug("Audit log saved: {} - {}", entry.eventType(), entry.userId());
        return Uni.createFrom().voidItem();
    }
}
