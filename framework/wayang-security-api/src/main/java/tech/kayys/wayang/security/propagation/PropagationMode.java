package tech.kayys.wayang.security.propagation;

/**
 * Defines how security authority is propagated to a downstream invocation.
 */
public enum PropagationMode {

    /**
     * The same security context is propagated as-is.
     * Use sparingly — should only be used when the downstream target is fully trusted.
     */
    PROPAGATE,

    /**
     * An attenuated child delegation is created and propagated.
     * This is the normal mode for Agent → Skill, Agent → Tool, Agent → Child Agent calls.
     */
    DELEGATE,

    /**
     * No security context is propagated.
     * The downstream invocation runs as anonymous or with a minimal context.
     * Useful for untrusted sandboxes, anonymous computations, or public metadata services.
     */
    ISOLATE
}
