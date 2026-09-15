package tech.kayys.wayang.coordination;

/**
 * High-level coordination topology used to organize multiple agents.
 *
 * <p>A pattern describes how agents are organized and coordinate. It does not
 * describe how an individual model is selected or how workflow nodes are executed.</p>
 */
public enum CoordinationPattern {

    /**
     * A central coordinator delegates work to participating agents.
     */
    CENTRALIZED,

    /**
     * Agents are organized into multiple coordination levels (e.g. manager and worker).
     */
    HIERARCHICAL,

    /**
     * Agents coordinate autonomously without a single permanent coordinator.
     */
    DECENTRALIZED,

    /**
     * Agents dynamically collaborate as a peer-to-peer swarm towards shared objectives.
     */
    SWARM,

    /**
     * Sequential or staged processing pipeline across agents.
     */
    PIPELINE,

    /**
     * Shared state/context board where agents read and post observations asynchronously.
     */
    BLACKBOARD,

    /**
     * Trigger and event-based coordination between agents.
     */
    EVENT_DRIVEN,

    /**
     * Role-based agent allocation and routing.
     */
    ROLE_BASED,

    /**
     * Coordination topology composed from multiple patterns.
     */
    HYBRID
}
