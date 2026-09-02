package tech.kayys.wayang.knowledge.audit;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAuditStore implements KnowledgeAuditStore {

    private final Map<String, KnowledgeAuditEvent> events = new ConcurrentHashMap<>();

    @Override
    public void publish(KnowledgeAuditEvent event) {
        if (event == null || event.id() == null) {
            throw new IllegalArgumentException("event and event.id are required");
        }
        events.put(event.id(), event);
    }

    @Override
    public List<KnowledgeAuditEvent> query(KnowledgeAuditQuery query) {
        if (query == null) {
            return List.of();
        }

        if (query.tenantId() == null || query.tenantId().isBlank()) {
            throw new IllegalArgumentException("tenantId is required for audit queries");
        }

        return events.values().stream()
                .filter(e -> query.tenantId().equals(e.tenantId()))
                .filter(e -> query.workspaceId() == null || query.workspaceId().equals(e.workspaceId()))
                .filter(e -> query.projectId() == null || query.projectId().equals(e.projectId()))
                .filter(e -> query.agentId() == null || query.agentId().equals(e.agentId()))
                .filter(e -> query.executionId() == null || query.executionId().equals(e.executionId()))
                .filter(e -> query.operation() == null || query.operation().equals(e.operation()))
                .filter(e -> query.from() == null || !e.createdAt().isBefore(query.from()))
                .filter(e -> query.until() == null || !e.createdAt().isAfter(query.until()))
                .sorted(Comparator.comparing(KnowledgeAuditEvent::createdAt).reversed())
                .limit(query.limit())
                .toList();
    }
}
