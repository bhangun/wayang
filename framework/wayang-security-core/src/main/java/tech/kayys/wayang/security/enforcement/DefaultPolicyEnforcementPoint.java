package tech.kayys.wayang.security.enforcement;

import tech.kayys.wayang.security.authz.AuthorizationDecision;
import tech.kayys.wayang.security.authz.AuthorizationRequest;
import tech.kayys.wayang.security.authz.AuthorizationService;
import tech.kayys.wayang.security.obligation.*;
import tech.kayys.wayang.security.transform.DataTransformer;
import tech.kayys.wayang.security.transform.TransformationOperation;
import tech.kayys.wayang.security.transform.TransformationRequest;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DefaultPolicyEnforcementPoint implements PolicyEnforcementPoint {

    private final AuthorizationService authorizationService;
    private final ObligationPipeline obligationPipeline;
    private final DataTransformer dataTransformer;

    public DefaultPolicyEnforcementPoint(
            AuthorizationService authorizationService,
            ObligationPipeline obligationPipeline,
            DataTransformer dataTransformer
    ) {
        this.authorizationService = Objects.requireNonNull(authorizationService, "authorizationService");
        this.obligationPipeline   = Objects.requireNonNull(obligationPipeline, "obligationPipeline");
        this.dataTransformer      = Objects.requireNonNull(dataTransformer, "dataTransformer");
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<EnforcementResult> enforce(EnforcementRequest request) {
        AuthorizationRequest authReq = new AuthorizationRequest(
                request.securityContext(),
                request.capability(),
                request.action(),
                request.resource(),
                request.attributes()
        );

        return authorizationService.authorize(authReq).thenCompose(decision -> {
            if (!decision.allowed()) {
                return CompletableFuture.completedFuture(
                        EnforcementResult.deny("Access denied: " + decision.reason())
                );
            }

            // Extract obligations from decision
            List<Obligation> obligations = extractObligations(decision);

            ObligationContext obCtx = ObligationContext.of(
                    request.securityContext(),
                    Map.of(
                            "capability", request.capability() != null ? request.capability() : "",
                            "action",     request.action()     != null ? request.action()     : ""
                    )
            );

            return obligationPipeline.execute(obligations, obCtx).thenCompose(pipelineResult -> {
                if (!pipelineResult.continueExecution()) {
                    String error = (String) pipelineResult.attributes().getOrDefault("error", "Obligation stopped execution");
                    return CompletableFuture.completedFuture(EnforcementResult.deny(error));
                }

                // Check for data transformation obligations (e.g. REDACT or MASK)
                return applyDataTransformations(request.data(), obligations).thenApply(transformedData ->
                        EnforcementResult.allow(transformedData, pipelineResult.attributes())
                );
            });
        });
    }

    @SuppressWarnings("unchecked")
    private List<Obligation> extractObligations(AuthorizationDecision decision) {
        Object val = decision.obligations().get("obligations");
        if (val instanceof List<?> list) {
            return (List<Obligation>) list;
        }
        return List.of();
    }

    private CompletionStage<Object> applyDataTransformations(Object data, List<Obligation> obligations) {
        if (data == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletionStage<Object> stage = CompletableFuture.completedFuture(data);

        for (Obligation ob : obligations) {
            TransformationOperation op = null;
            if (StandardObligations.REDACT.equals(ob.type())) {
                op = TransformationOperation.REDACT;
            } else if (StandardObligations.MASK.equals(ob.type())) {
                op = TransformationOperation.MASK;
            }

            if (op != null) {
                final TransformationOperation finalOp = op;
                stage = stage.thenCompose(currentData ->
                        dataTransformer.transform(TransformationRequest.of(currentData, finalOp, ob.parameters()))
                                .thenApply(tech.kayys.wayang.security.transform.TransformationResult::data)
                );
            }
        }

        return stage;
    }
}
