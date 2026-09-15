package tech.kayys.wayang.security.obligation.audit;

import java.util.concurrent.CompletionStage;

/**
 * SPI for persisting audit records.
 */
public interface AuditSink {

    CompletionStage<Void> write(AuditEvent event);
}
