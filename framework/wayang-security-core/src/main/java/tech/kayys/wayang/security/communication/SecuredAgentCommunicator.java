package tech.kayys.wayang.security.communication;

import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.security.authz.AuthorizationRequest;
import tech.kayys.wayang.security.authz.AuthorizationDecision;
import tech.kayys.wayang.security.authz.AuthorizationService;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.exception.AuthorizationException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * A decorator around {@link AgentCommunicator} that enforces authorization
 * before delegating to the underlying communicator.
 *
 * <pre>
 * A2A ───────┐
 *            │
 * ANP ───────┼──► SecuredAgentCommunicator ──► AgentCommunicator ──► Agent
 *            │
 * Local ─────┘
 * </pre>
 *
 * The {@link SecurityContext} is extracted from the {@link ProtocolContext} attributes.
 * If absent, {@link SecurityContext#anonymous()} is used.
 */
public final class SecuredAgentCommunicator implements AgentCommunicator {

    /** Key used to store/retrieve {@link SecurityContext} inside {@link ProtocolContext} attributes. */
    public static final String SECURITY_CONTEXT_KEY = "wayang.security.context";

    private final AgentCommunicator delegate;
    private final AuthorizationService authorizationService;

    public SecuredAgentCommunicator(
            AgentCommunicator delegate,
            AuthorizationService authorizationService
    ) {
        this.delegate             = Objects.requireNonNull(delegate,             "delegate");
        this.authorizationService = Objects.requireNonNull(authorizationService, "authorizationService");
    }

    @Override
    public CompletionStage<AgentResponse> send(AgentRequest request, ProtocolContext context) {
        return authorize(request, context).thenCompose(decision -> {
            if (!decision.allowed()) {
                return CompletableFuture.failedFuture(
                        new AuthorizationException("Request denied: " + decision.reason())
                );
            }
            return delegate.send(request, context);
        });
    }

    @Override
    public AgentTask submit(AgentRequest request, ProtocolContext context) {
        AuthorizationDecision decision = authorizeSync(request, context);
        if (!decision.allowed()) {
            throw new AuthorizationException("Request denied: " + decision.reason());
        }
        return delegate.submit(request, context);
    }

    @Override
    public Flow.Publisher<AgentEvent> stream(AgentRequest request, ProtocolContext context) {
        AuthorizationDecision decision = authorizeSync(request, context);
        if (!decision.allowed()) {
            throw new AuthorizationException("Request denied: " + decision.reason());
        }
        return delegate.stream(request, context);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private CompletionStage<AuthorizationDecision> authorize(AgentRequest request, ProtocolContext context) {
        SecurityContext secCtx = extractSecurityContext(context);
        AuthorizationRequest authRequest = AuthorizationRequest.execute(
                secCtx,
                request.capability() != null ? request.capability().value() : null
        );
        return authorizationService.authorize(authRequest);
    }

    private AuthorizationDecision authorizeSync(AgentRequest request, ProtocolContext context) {
        try {
            return authorize(request, context).toCompletableFuture().join();
        } catch (java.util.concurrent.CompletionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new AuthorizationException("Authorization failed: " + cause.getMessage(), cause);
        }
    }

    private SecurityContext extractSecurityContext(ProtocolContext context) {
        if (context == null) {
            return SecurityContext.anonymous();
        }
        Object value = context.attributes().get(SECURITY_CONTEXT_KEY);
        if (value instanceof SecurityContext sc) {
            return sc;
        }
        return SecurityContext.anonymous();
    }
}
