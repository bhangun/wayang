package tech.kayys.wayang.plugin.node;

/**
 * Validation warning
 */
record ValidationWarning(
    String field,
    String code,
    String message
) {}