package tech.kayys.wayang.knowledge.audit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAuditSink implements KnowledgeAuditSink {

    private final Map<String, KnowledgeAuditEvent> events = new ConcurrentHashMap<>();

    @Override
    public void publish(KnowledgeAuditEvent event) {
        if (event == null || event.id() == null) {
            throw new IllegalArgumentException("event and event.id are required");
        }
        events.put(event.id(), event);
    }

    public List<KnowledgeAuditEvent> events() {
        return List.copyOf(events.values());
    }
}
