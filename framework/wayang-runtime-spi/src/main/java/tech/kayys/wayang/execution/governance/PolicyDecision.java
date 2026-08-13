package tech.kayys.wayang.execution.governance;

/**
 * Sealed result from a {@link ToolPolicy} evaluation (§30 — Policy Engine).
 * Execution kernel switches on the concrete type to determine next action.
 */
public sealed interface PolicyDecision
        permits PolicyDecision.Allow, PolicyDecision.Deny, PolicyDecision.RequireApproval {

    /** The tool call is permitted. Proceed with execution. */
    record Allow() implements PolicyDecision {}

    /** The tool call is denied. Do not execute; return failure to the agent. */
    record Deny(String reason, String policyId) implements PolicyDecision {}

    /** The tool call requires explicit human or orchestrator approval before executing. */
    record RequireApproval(String reason, String policyId) implements PolicyDecision {}

    // --- Convenience factories ---

    static PolicyDecision allow() { return new Allow(); }

    static PolicyDecision deny(String reason, String policyId) {
        return new Deny(reason, policyId);
    }

    static PolicyDecision requireApproval(String reason, String policyId) {
        return new RequireApproval(reason, policyId);
    }

    /** Returns true for the happy path. */
    default boolean isAllowed() { return this instanceof Allow; }
}
