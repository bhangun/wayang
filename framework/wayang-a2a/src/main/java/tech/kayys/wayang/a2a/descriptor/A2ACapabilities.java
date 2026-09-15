package tech.kayys.wayang.a2a.descriptor;

/**
 * Flags indicating which A2A 1.0 features this agent supports.
 */
public record A2ACapabilities(
        boolean streaming,
        boolean pushNotifications,
        boolean stateTransitionHistory,
        boolean extendedAgentCard
) {

    public static A2ACapabilities defaults() {
        return new A2ACapabilities(true, true, true, false);
    }
}
