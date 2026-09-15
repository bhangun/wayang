package tech.kayys.wayang.security.context;

import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.util.Optional;

/**
 * Transport-neutral carrier for a {@link SecurityContextSnapshot}.
 *
 * <p>Decouples the security context from transport mechanisms (HTTP headers,
 * thread-locals, reactive context, etc.). Implementations include:
 * <ul>
 *   <li>{@code ThreadLocalSecurityContextCarrier} — synchronous/blocking</li>
 *   <li>{@code ReactiveSecurityContextCarrier} — Mutiny/CompletionStage</li>
 *   <li>{@code QuarkusSecurityContextCarrier} — CDI-scoped</li>
 * </ul>
 */
public interface SecurityContextCarrier {

    /** Associates the given snapshot with this carrier. */
    void set(SecurityContextSnapshot context);

    /** Returns the current snapshot, if any. */
    Optional<SecurityContextSnapshot> get();

    /** Removes the current snapshot from this carrier. */
    void clear();
}
