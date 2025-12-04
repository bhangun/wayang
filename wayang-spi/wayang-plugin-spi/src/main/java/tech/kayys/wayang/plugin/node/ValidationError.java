package tech.kayys.wayang.plugin.node;

/**
 * Validation error
 */
record ValidationError(
    String field,
    String code,
    String message,
    Object rejectedValue
) {}
