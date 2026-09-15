package tech.kayys.wayang.anp.description;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing local ANP agent descriptions and serving {@code /.well-known/agent.json}.
 */
public final class AnpAgentDescriptionService {

    private final Map<String, AnpAgentDescription> descriptionsByDid = new ConcurrentHashMap<>();

    /** Registers the self-description of a local agent. */
    public void register(AnpAgentDescription description) {
        descriptionsByDid.put(description.did(), description);
    }

    /** Finds the description for a given DID:WBA. */
    public Optional<AnpAgentDescription> findByDid(String did) {
        return Optional.ofNullable(descriptionsByDid.get(did));
    }

    /** Returns all registered agent descriptions. */
    public Collection<AnpAgentDescription> all() {
        return descriptionsByDid.values();
    }
}
