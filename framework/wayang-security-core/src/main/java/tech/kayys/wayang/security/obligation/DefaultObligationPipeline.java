package tech.kayys.wayang.security.obligation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Sequential, fail-closed implementation of {@link ObligationPipeline}.
 * If any obligation fails, or if an unhandled obligation is encountered, execution halts immediately.
 */
public final class DefaultObligationPipeline implements ObligationPipeline {

    private final ObligationRegistry registry;

    public DefaultObligationPipeline(ObligationRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public CompletionStage<ObligationPipelineResult> execute(
            List<Obligation> obligations,
            ObligationContext context
    ) {
        if (obligations == null || obligations.isEmpty()) {
            return CompletableFuture.completedFuture(
                    ObligationPipelineResult.success(List.of())
            );
        }

        CompletableFuture<ObligationPipelineResult> chain =
                CompletableFuture.completedFuture(
                        new ObligationPipelineResult(true, true, new ArrayList<>(), Map.of())
                );

        for (Obligation obligation : obligations) {
            chain = chain.thenCompose(current -> {
                if (!current.continueExecution()) {
                    return CompletableFuture.completedFuture(current);
                }

                ObligationExecutor executor = registry.find(obligation.type()).orElse(null);
                if (executor == null) {
                    // Fail closed: an unhandled obligation must halt execution
                    return CompletableFuture.completedFuture(
                            ObligationPipelineResult.failure(
                                    "No executor registered for obligation: " + obligation.type().value(),
                                    current.results()
                            )
                    );
                }

                return executor.execute(obligation, context).thenApply(obligationResult -> {
                    List<ObligationResult> updated = new ArrayList<>(current.results());
                    updated.add(obligationResult);

                    boolean allSuccess = current.successful() && obligationResult.successful();
                    boolean shouldContinue = obligationResult.continueExecution() && allSuccess;

                    return new ObligationPipelineResult(
                            allSuccess,
                            shouldContinue,
                            List.copyOf(updated),
                            obligationResult.attributes()
                    );
                });
            });
        }

        return chain;
    }
}
