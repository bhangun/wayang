package tech.kayys.wayang.core.model;


/**
 * Audit statistics
 */
record AuditStatistics(
    int activeExecutions,
    double totalEvents
) {}