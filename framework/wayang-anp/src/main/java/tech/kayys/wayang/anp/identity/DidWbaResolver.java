package tech.kayys.wayang.anp.identity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves a DID:WBA identity into a {@link DidWbaDocument}.
 *
 * <p>Supports local registry registration for fast-path / offline resolution,
 * with async resolution for remote agents.
 */
public final class DidWbaResolver {

    private final Map<String, DidWbaDocument> registry = new ConcurrentHashMap<>();

    /** Registers a known DID document locally (e.g. for self or federated peers). */
    public void register(DidWbaDocument document) {
        registry.put(document.id(), document);
    }

    /** Resolves a DID document. Checks local registry first. */
    public CompletionStage<Optional<DidWbaDocument>> resolve(DidWbaIdentity identity) {
        DidWbaDocument doc = registry.get(identity.raw());
        if (doc != null) {
            return CompletableFuture.completedFuture(Optional.of(doc));
        }
        // In full network mode, this would invoke HTTP GET on identity.toDidDocumentUrl()
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
