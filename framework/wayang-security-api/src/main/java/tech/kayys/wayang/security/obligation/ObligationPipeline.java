package tech.kayys.wayang.security.obligation;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Pipeline coordinating the sequential execution of obligations across lifecycle phases.
 */
public interface ObligationPipeline {

    CompletionStage<ObligationPipelineResult> execute(
            List<Obligation> obligations,
            ObligationContext context
    );
}
