package tech.kayys.wayang.context.api.model;

/**
 * What kind of change the target file is being edited for. Deliberately
 * small and code-specific -- not an attempt at a general cross-domain intent
 * taxonomy (that's a much bigger, unproven abstraction; see ContextPlanner's
 * javadoc for why it's out of scope here).
 */
public enum TaskIntent {
    BUG_FIX,
    REFACTOR,
    NEW_FEATURE,
    EXPLORATION
}
