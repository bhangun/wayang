package tech.kayys.wayang.security.obligation;

import java.util.concurrent.CompletionStage;

/**
 * SPI for executing an obligation.
 */
public interface ObligationExecutor {

    ObligationType type();

    CompletionStage<ObligationResult> execute(Obligation obligation, ObligationContext context);
}
