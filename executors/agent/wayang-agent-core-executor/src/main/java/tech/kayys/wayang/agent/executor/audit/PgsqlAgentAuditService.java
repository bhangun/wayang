package tech.kayys.wayang.agent.executor.audit;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.core.audit.AgentArtifact;
import tech.kayys.wayang.agent.core.audit.AgentAuditService;

/**
 * Stores agent artifacts into a PostgreSQL database using Hibernate Reactive Panache.
 */
@ApplicationScoped
public class PgsqlAgentAuditService implements AgentAuditService {

    private static final Logger LOG = LoggerFactory.getLogger(PgsqlAgentAuditService.class);

    @Override
    public Uni<Void> saveArtifact(AgentArtifact artifact) {
        AgentAuditRecord record = new AgentAuditRecord();
        record.id = artifact.id();
        record.runId = artifact.runId();
        record.tenantId = artifact.tenantId();
        record.type = artifact.type();
        record.content = artifact.content();
        record.format = artifact.format();
        record.createdAt = artifact.createdAt();

        return Panache.withTransaction(record::persist)
                .onItem().invoke(() -> LOG.debug("Saved audit artifact {} to pgsql", artifact.id()))
                .onFailure().invoke(e -> LOG.error("Failed to save audit artifact to pgsql", e))
                .replaceWithVoid();
    }

    @Override
    public Uni<AgentArtifact> getArtifact(String artifactId, String tenantId) {
        return AgentAuditRecord.<AgentAuditRecord>findById(artifactId)
                .map(record -> {
                    if (record == null || !record.tenantId.equals(tenantId)) {
                        return null;
                    }
                    return mapToArtifact(record);
                });
    }

    @Override
    public Uni<List<AgentArtifact>> getArtifactsByRun(String runId, String tenantId) {
        return AgentAuditRecord.<AgentAuditRecord>find("runId = ?1 and tenantId = ?2", runId, tenantId)
                .list()
                .map(records -> records.stream()
                        .map(this::mapToArtifact)
                        .collect(Collectors.toList()));
    }

    private AgentArtifact mapToArtifact(AgentAuditRecord record) {
        return new AgentArtifact(
                record.id,
                record.runId,
                record.tenantId,
                record.type,
                record.content,
                record.format,
                java.util.Map.of(), // Metadata serialization could be added later if needed
                record.createdAt
        );
    }
}
