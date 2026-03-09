package tech.kayys.wayang.agent;

/**
 * Agent Status - Unified enum for control plane and executors.
 */
public enum AgentStatus {
    AVAILABLE,
    BUSY,
    UNAVAILABLE,
    ERROR,
    MAINTENANCE,
    INACTIVE,
    ACTIVE,
    SUSPENDED
}
