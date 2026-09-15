package tech.kayys.wayang.anp.discovery;

import tech.kayys.wayang.anp.description.AnpAgentDescription;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for discovering remote ANP agents via {@code /.well-known/agent.json}.
 */
public final class AnpDiscoveryClient {

    private final Map<String, AnpAgentDescription> cache = new ConcurrentHashMap<>();

    /** Registers a cached remote description (for offline testing or static peering). */
    public void registerCached(AnpAgentDescription description) {
        cache.put(description.did(), description);
    }

    /** Fetches the agent description for a target DID:WBA. */
    public CompletionStage<Optional<AnpAgentDescription>> fetch(DidWbaIdentity did) {
        AnpAgentDescription desc = cache.get(did.raw());
        if (desc != null) {
            return CompletableFuture.completedFuture(Optional.of(desc));
        }
        // In full network deployment: java.net.http.HttpClient GET did.toAgentDescriptionUrl()
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
