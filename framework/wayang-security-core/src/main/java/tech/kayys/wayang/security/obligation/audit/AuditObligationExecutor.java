package tech.kayys.wayang.security.obligation.audit;

import tech.kayys.wayang.security.obligation.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public final class AuditObligationExecutor implements ObligationExecutor {

    private final AuditSink sink;

    public AuditObligationExecutor(AuditSink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    @Override
    public ObligationType type() {
        return StandardObligations.AUDIT;
    }

    @Override
    public CompletionStage<ObligationResult> execute(Obligation obligation, ObligationContext context) {
        String action = (String) context.attributes().getOrDefault("action", "execute");
        String capability = (String) context.attributes().getOrDefault("capability", "unknown");

        AuditEvent event = new AuditEvent(
                java.time.Instant.now(),
                action,
                capability,
                context.securityContext().principal(),
                context.securityContext().tenant().tenantId(),
                obligation.parameters()
        );

        return sink.write(event).thenApply(v -> ObligationResult.success(Map.of("audited", true)));
    }
}
