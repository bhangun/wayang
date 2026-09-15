package tech.kayys.wayang.security.obligation;

import java.util.Optional;

/**
 * Registry mapping {@link ObligationType} to corresponding {@link ObligationExecutor}s.
 */
public interface ObligationRegistry {

    Optional<ObligationExecutor> find(ObligationType type);

    void register(ObligationExecutor executor);
}
