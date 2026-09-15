package tech.kayys.wayang.security.delegation;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Delegation state tracking authority attenuation and provenance across agent hops.
 */
public record DelegationContext(
        DelegationId id,
        DelegationAudience audience,
        DelegationConstraints constraints,
        Instant issuedAt,
        Instant expiresAt,
        Optional<DelegationId> parent
) {

    public DelegationContext {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(audience, "audience");
        Objects.requireNonNull(constraints, "constraints");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        parent = parent == null ? Optional.empty() : parent;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public static DelegationContext create(DelegationAudience audience, DelegationConstraints constraints, long ttlSeconds) {
        Instant now = Instant.now();
        return new DelegationContext(
                DelegationId.generate(),
                audience,
                constraints,
                now,
                now.plusSeconds(ttlSeconds),
                Optional.empty()
        );
    }
}
