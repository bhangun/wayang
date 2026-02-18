package tech.kayys.wayang.prompt.core;

/**
 * ============================================================================
 * ValidationWarningType — classification of validation warnings.
 * ============================================================================
 *
 * Used by the visual editor to categorize issues and suggest fixes.
 */
public enum ValidationWarningType {
    /**
     * A variable is declared in the template's schema but never appears
     * as a placeholder in the template body.
     */
    DECLARED_BUT_MISSING,

    /**
     * A {{name}} placeholder appears in the template body but has no
     * corresponding variable declaration in the schema.
     */
    PLACEHOLDER_UNDECLARED
}