package tech.kayys.wayang.security.transform;

import tech.kayys.wayang.security.obligation.*;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * An obligation executor that marks data transformation obligations (such as REDACT and MASK)
 * as handled during pipeline evaluation so that the PEP can apply the transformation.
 */
public final class TransformObligationExecutor implements ObligationExecutor {

    private final ObligationType type;

    public TransformObligationExecutor(ObligationType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public static TransformObligationExecutor of(ObligationType type) {
        return new TransformObligationExecutor(type);
    }

    @Override
    public ObligationType type() {
        return type;
    }

    @Override
    public CompletionStage<ObligationResult> execute(Obligation obligation, ObligationContext context) {
        return CompletableFuture.completedFuture(ObligationResult.success());
    }
}
