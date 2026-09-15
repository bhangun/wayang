package tech.kayys.wayang.security.delegation;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDelegationServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private final DelegationAttenuator attenuator = new DefaultDelegationAttenuator();
    private final DelegationPolicy allowAllPolicy = (parent, req) -> true;
    private final DelegationService service = new DefaultDelegationService(attenuator, allowAllPolicy, clock);

    @Test
    void delegatesSuccessfullyWithinParentAuthority() throws Exception {
        DelegationConstraints parentConstraints = new DelegationConstraints(
                List.of("read", "write"), List.of("exec"), 3
        );
        DelegationContext parentDelegation = new DelegationContext(
                DelegationId.generate(),
                DelegationAudience.of("agent-a"),
                parentConstraints,
                clock.instant(),
                clock.instant().plus(Duration.ofHours(2)),
                Optional.empty()
        );

        SecurityContextSnapshot parent = new SecurityContextSnapshot(
                Principal.anonymous(),
                TenantContext.empty(),
                Optional.of(parentDelegation),
                Map.of()
        );

        DelegationRequest request = new DelegationRequest(
                DelegationAudience.of("agent-b"),
                new DelegationConstraints(List.of("read"), List.of("exec"), 2),
                Duration.ofHours(1),
                Map.of()
        );

        DelegationContext child = service.delegate(parent, request).toCompletableFuture().get();

        assertNotNull(child);
        assertEquals(DelegationAudience.of("agent-b"), child.audience());
        assertEquals(List.of("read"), child.constraints().allowedCapabilities());
        assertEquals(List.of("exec"), child.constraints().allowedActions());
        assertEquals(2, child.constraints().maxHops());
        assertEquals(Optional.of(parentDelegation.id()), child.parent());
    }

    @Test
    void rejectsWhenChildExceedsParentLifetime() {
        DelegationContext parentDelegation = new DelegationContext(
                DelegationId.generate(),
                DelegationAudience.of("agent-a"),
                DelegationConstraints.unrestricted(),
                clock.instant(),
                clock.instant().plus(Duration.ofMinutes(30)),
                Optional.empty()
        );

        SecurityContextSnapshot parent = new SecurityContextSnapshot(
                Principal.anonymous(),
                TenantContext.empty(),
                Optional.of(parentDelegation),
                Map.of()
        );

        DelegationRequest request = new DelegationRequest(
                DelegationAudience.of("agent-b"),
                DelegationConstraints.unrestricted(),
                Duration.ofHours(1), // Longer than 30m parent!
                Map.of()
        );

        assertThrows(Exception.class, () -> service.delegate(parent, request).toCompletableFuture().get());
    }
}
