package tech.kayys.wayang.control.dto;

/**
 * Types of safety guardrails.
 */
public enum GuardrailType {
    PII_FILTER,
    TOXICITY_CHECK,
    BIAS_DETECTION,
    COST_CONTROL,
    THROTTLE,
    CONTENT_MODERATION
}
