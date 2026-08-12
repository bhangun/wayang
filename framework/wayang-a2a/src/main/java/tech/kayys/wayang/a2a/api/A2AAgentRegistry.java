package tech.kayys.wayang.a2a.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import tech.kayys.wayang.a2a.model.AgentCard;

/**
 * Registry for discovering and managing remote agent capabilities via A2A.
 */
public interface A2AAgentRegistry {

    /**
     * Discovers an agent card by interrogating a remote endpoint.
     * @param endpoint The URI of the remote agent.
     * @return A CompletionStage resolving to the AgentCard.
     */
    CompletionStage<AgentCard> discover(URI endpoint);

    /**
     * Registers an agent card in the local registry.
     * @param card The AgentCard to register.
     * @return A CompletionStage resolving to the registered AgentCard.
     */
    CompletionStage<AgentCard> register(AgentCard card);

    /**
     * Finds a previously registered agent card by ID.
     * @param agentId The ID of the agent.
     * @return An Optional containing the AgentCard if found.
     */
    Optional<AgentCard> find(String agentId);

    /**
     * Finds all agents that declare a specific capability.
     * @param capability The capability to search for.
     * @return A list of matching AgentCards.
     */
    List<AgentCard> findByCapability(String capability);

    /**
     * Finds all agents that declare a specific skill.
     * @param skill The skill to search for.
     * @return A list of matching AgentCards.
     */
    List<AgentCard> findBySkill(String skill);
}
