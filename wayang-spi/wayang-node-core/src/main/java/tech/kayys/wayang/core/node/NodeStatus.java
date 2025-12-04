package tech.kayys.wayang.core.node;

/**
 * Node lifecycle status
 */
enum NodeStatus {
    PENDING,
    SCANNING,
    APPROVED,
    REVOKED,
    DEPRECATED
}