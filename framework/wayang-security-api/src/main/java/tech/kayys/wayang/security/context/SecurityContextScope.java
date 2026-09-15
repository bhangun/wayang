package tech.kayys.wayang.security.context;

import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

/**
 * An auto-closeable scope that sets and then clears a security context
 * in a {@link SecurityContextCarrier}.
 *
 * <p>Usage:
 * <pre>{@code
 * try (SecurityContextScope scope = SecurityContextScope.of(carrier, snapshot)) {
 *     // code runs with security context in scope
 * }
 * }</pre>
 */
public final class SecurityContextScope implements AutoCloseable {

    private final SecurityContextCarrier carrier;

    private SecurityContextScope(SecurityContextCarrier carrier, SecurityContextSnapshot snapshot) {
        this.carrier = carrier;
        carrier.set(snapshot);
    }

    public static SecurityContextScope of(SecurityContextCarrier carrier, SecurityContextSnapshot snapshot) {
        return new SecurityContextScope(carrier, snapshot);
    }

    @Override
    public void close() {
        carrier.clear();
    }
}
