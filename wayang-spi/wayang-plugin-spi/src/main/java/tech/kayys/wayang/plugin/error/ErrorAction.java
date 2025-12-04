package tech.kayys.wayang.plugin.error;

public enum ErrorAction {
    RETRY,
    AUTO_FIX,
    HUMAN_REVIEW,
    FALLBACK,
    ABORT, DELEGATE_TO_PLATFORM, ESCALATE,
    CUSTOM_RECOVERY
}