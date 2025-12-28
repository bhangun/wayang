package tech.kayys.wayang.engine;

/**
 * Escalation policy for human tasks.
 */
public enum EscalationPolicy {
    NOTIFY_SUPERVISOR, // Send notification to supervisor
    AUTO_APPROVE_ON_TIMEOUT, // Automatically approve after SLA breach
    AUTO_REJECT_ON_TIMEOUT, // Automatically reject after SLA breach
    REASSIGN_TO_POOL, // Reassign to operator pool
    PAUSE_WORKFLOW // Pause workflow until manual intervention
}
