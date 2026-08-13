package tech.kayys.wayang.execution.governance;

/**
 * Canonical capability levels for tool governance (§29 — Capability Model).
 *
 * <p>Ordered from least to most privileged. A policy can require a minimum
 * level (e.g. NETWORK) without enumerating every individual tool.</p>
 */
public enum ToolCapabilityLevel {

    /** Pure read: queries, lookups, observations. No state change. */
    READ,

    /** Writes to external systems: DB upserts, API POSTs. */
    WRITE,

    /** General code/command execution. Superset of READ + WRITE. */
    EXECUTE,

    /** Network I/O: HTTP calls, DNS, socket connections. */
    NETWORK,

    /** Local filesystem access: read or write files. */
    FILESYSTEM,

    /** Shell/process execution on the host OS. */
    SHELL,

    /** System-level or infrastructure operations (container, cloud API, etc.). */
    SYSTEM;

    /**
     * Returns true if this level is at least as privileged as {@code required}.
     */
    public boolean isAtLeast(ToolCapabilityLevel required) {
        return this.ordinal() >= required.ordinal();
    }
}
