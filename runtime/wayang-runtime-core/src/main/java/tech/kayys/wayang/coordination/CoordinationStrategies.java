package tech.kayys.wayang.coordination;

/**
 * Well-known coordination strategy identifiers.
 */
public final class CoordinationStrategies {

    private CoordinationStrategies() {
    }

    public static final String CENTRALIZED = "centralized";
    public static final String HIERARCHICAL = "hierarchical";
    public static final String DECENTRALIZED = "decentralized";
    public static final String SWARM = "swarm";
    public static final String PIPELINE = "pipeline";
    public static final String BLACKBOARD = "blackboard";
    public static final String EVENT_DRIVEN = "event-driven";
    public static final String ROLE_BASED = "role-based";
    public static final String HYBRID = "hybrid";
}
