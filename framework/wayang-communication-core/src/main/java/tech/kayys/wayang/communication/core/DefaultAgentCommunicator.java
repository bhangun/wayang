package tech.kayys.wayang.communication.core;

import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.core.protocol.ProtocolCandidate;
import tech.kayys.wayang.communication.core.protocol.ProtocolRouter;
import tech.kayys.wayang.communication.exception.CommunicationException;
import tech.kayys.wayang.communication.protocol.ProtocolContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public final class DefaultAgentCommunicator implements AgentCommunicator {

    private final ProtocolRouter router;

    public DefaultAgentCommunicator(ProtocolRouter router) {
        this.router = Objects.requireNonNull(router, "router");
    }

    @Override
    public CompletionStage<AgentResponse> send(
            AgentRequest request,
            ProtocolContext context
    ) {
        var ctx = context != null ? context : ProtocolContext.empty();
        var candidates = router.candidates(request, ctx);

        if (candidates.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new CommunicationException(
                            "No compatible protocol for agent " + request.target().id()
                    )
            );
        }

        return tryCandidate(candidates, 0, request, ctx, null);
    }

    private CompletionStage<AgentResponse> tryCandidate(
            List<ProtocolCandidate> candidates,
            int index,
            AgentRequest request,
            ProtocolContext context,
            Throwable lastError
    ) {
        if (index >= candidates.size()) {
            return CompletableFuture.failedFuture(
                    new CommunicationException(
                            "All candidate communication protocols failed for agent " + request.target().id(),
                            lastError
                    )
            );
        }

        var candidate = candidates.get(index);
        boolean allowFallback = request.communication() == null || request.communication().allowFallback();

        try {
            var client = candidate.protocol().client(context);
            return client.send(request, context).handle((response, error) -> {
                if (error == null) {
                    return CompletableFuture.completedFuture(response);
                }

                Throwable actualError = error instanceof java.util.concurrent.CompletionException && error.getCause() != null
                        ? error.getCause()
                        : error;

                if (!allowFallback || index == candidates.size() - 1) {
                    return CompletableFuture.<AgentResponse>failedFuture(actualError);
                }

                return tryCandidate(candidates, index + 1, request, context, actualError);
            }).thenCompose(stage -> stage);
        } catch (Throwable error) {
            Throwable actualError = error instanceof java.util.concurrent.CompletionException && error.getCause() != null
                    ? error.getCause()
                    : error;
            if (!allowFallback || index == candidates.size() - 1) {
                return CompletableFuture.failedFuture(actualError);
            }
            return tryCandidate(candidates, index + 1, request, context, actualError);
        }
    }

    @Override
    public AgentTask submit(
            AgentRequest request,
            ProtocolContext context
    ) {
        var ctx = context != null ? context : ProtocolContext.empty();
        var candidates = router.candidates(request, ctx);

        if (candidates.isEmpty()) {
            throw new CommunicationException(
                    "No compatible protocol for agent " + request.target().id()
            );
        }

        return candidates.getFirst()
                .protocol()
                .client(ctx)
                .submit(request, ctx);
    }

    @Override
    public Flow.Publisher<AgentEvent> stream(
            AgentRequest request,
            ProtocolContext context
    ) {
        var ctx = context != null ? context : ProtocolContext.empty();
        var candidates = router.candidates(request, ctx);

        if (candidates.isEmpty()) {
            throw new CommunicationException(
                    "No compatible streaming protocol for agent " + request.target().id()
            );
        }

        return candidates.getFirst()
                .protocol()
                .client(ctx)
                .stream(request, ctx);
    }
}
