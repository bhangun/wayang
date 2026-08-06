package tech.kayys.wayang.execution;

/**
 * Variable scope.
 */
public enum VariableScope {
    GLOBAL("Available across all sessions"),
    SESSION("Available within the session"),
    EXECUTION("Available within the execution"),
    NODE("Available within the node"),
    LOCAL("Local to the current scope");

    private final String description;

    VariableScope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMoreRestrictiveThan(VariableScope other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean isMorePermissiveThan(VariableScope other) {
        return this.ordinal() < other.ordinal();
    }
}