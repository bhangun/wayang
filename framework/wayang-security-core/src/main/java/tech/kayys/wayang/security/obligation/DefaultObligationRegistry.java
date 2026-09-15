package tech.kayys.wayang.security.obligation;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe concurrent implementation of {@link ObligationRegistry}.
 */
public final class DefaultObligationRegistry implements ObligationRegistry {

    private final ConcurrentMap<String, ObligationExecutor> executors = new ConcurrentHashMap<>();

    @Override
    public Optional<ObligationExecutor> find(ObligationType type) {
        if (type == null) return Optional.empty();
        return Optional.ofNullable(executors.get(type.value()));
    }

    @Override
    public void register(ObligationExecutor executor) {
        Objects.requireNonNull(executor, "executor");
        executors.put(executor.type().value(), executor);
    }
}
