package tech.kayys.wayang.core.model;

import java.util.Map;

/**
 * Connection test result
 */
record ConnectionTestResult(
    boolean success,
    String message,
    Map<String, Object> details
) {}