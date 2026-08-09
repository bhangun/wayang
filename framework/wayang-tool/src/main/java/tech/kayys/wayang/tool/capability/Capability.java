package tech.kayys.wayang.tool.capability;

/**
 * Defines a specific capability or dimension of a Tool.
 * Examples include RequiresNetwork, DomainScope("git"), etc.
 */
public interface Capability {
    /**
     * Unique identifier for this capability type (e.g., "network", "domain_scope").
     */
    String id();

    /**
     * Checks if this capability satisfies another requested capability.
     * 
     * @param requested the capability being requested by an agent.
     * @return true if this capability satisfies the requested one.
     */
    boolean satisfies(Capability requested);

    /**
     * Indicates whether this capability inherently requires human approval 
     * before execution (e.g. destructive actions, billing-related actions).
     * @return true if approval is required.
     */
    default boolean requiresApproval() {
        return false;
    }
}
