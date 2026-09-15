package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.execution.*;
import tech.kayys.wayang.security.delegation.DelegationContext;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default {@link AgentInvocationFactory} that creates root and child invocations
 * using the configured ID generator and token factory.
 */
public final class DefaultAgentInvocationFactory implements AgentInvocationFactory {

    private final ExecutionIdGenerator idGenerator;
    private final CancellationTokenFactory tokenFactory;

    public DefaultAgentInvocationFactory(
            ExecutionIdGenerator idGenerator,
            CancellationTokenFactory tokenFactory) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.tokenFactory = Objects.requireNonNull(tokenFactory, "tokenFactory");
    }

    @Override
    public AgentInvocation root(AgentRequest request, SecurityContextSnapshot security) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(security, "security");

        String execId = idGenerator.generate();
        String traceId = idGenerator.generateTrace();

        ExecutionMetadata metadata = new ExecutionMetadata(
                execId, null, traceId, Instant.now(), Map.of()
        );
        CancellationToken token = tokenFactory.create();
        DefaultExecutionContext ctx = new DefaultExecutionContext(
                metadata, token, security, ExecutionLifecycle.CREATED
        );
        return new DefaultAgentInvocation(request, ctx);
    }

    @Override
    public AgentInvocation child(
            AgentInvocation parent,
            AgentRequest request,
            DelegationContext delegation) {
        Objects.requireNonNull(parent, "parent");
        Objects.requireNonNull(request, "request");

        String execId = idGenerator.generate();
        String parentId = parent.context().metadata().executionId();
        String traceId = parent.context().metadata().traceId();
        if (traceId == null) {
            traceId = idGenerator.generateTrace();
        }

        // Child uses attenuated security: same principal, new delegation
        SecurityContextSnapshot parentSecurity = parent.context().security();
        SecurityContextSnapshot childSecurity = new SecurityContextSnapshot(
                parentSecurity.principal(),
                parentSecurity.tenant(),
                Optional.ofNullable(delegation),
                parentSecurity.attributes()
        );

        ExecutionMetadata metadata = new ExecutionMetadata(
                execId, parentId, traceId, Instant.now(), Map.of()
        );
        CancellationToken token = tokenFactory.createChild(
                parent.context().cancellation()
        );
        DefaultExecutionContext ctx = new DefaultExecutionContext(
                metadata, token, childSecurity, ExecutionLifecycle.CREATED
        );
        return new DefaultAgentInvocation(request, ctx);
    }
}
