package tech.kayys.wayang.security.obligation.audit;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryAuditSink implements AuditSink {

    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public CompletionStage<Void> write(AuditEvent event) {
        events.add(event);
        return CompletableFuture.completedFuture(null);
    }

    public List<AuditEvent> events() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
