package tech.kayys.wayang.coordination;

import java.util.Objects;

/**
 * Coordination data binding descriptor for declarative inter-node data flow.
 */
public record CoordinationBinding(
        BindingSource source,
        String path,
        Object value
) {

    public CoordinationBinding {
        Objects.requireNonNull(source, "source must not be null");

        if (source.requiresPath() && (path == null || path.isBlank())) {
            throw new IllegalArgumentException("path is required for " + source);
        }
    }

    public enum BindingSource {
        STATIC(false),
        REQUEST(true),
        STEP_OUTPUT(true),
        EXECUTION_CONTEXT(true);

        private final boolean requiresPath;

        BindingSource(boolean requiresPath) {
            this.requiresPath = requiresPath;
        }

        public boolean requiresPath() {
            return requiresPath;
        }
    }

    public static CoordinationBinding staticValue(Object value) {
        return new CoordinationBinding(BindingSource.STATIC, null, value);
    }

    public static CoordinationBinding request(String path) {
        return new CoordinationBinding(BindingSource.REQUEST, path, null);
    }

    public static CoordinationBinding stepOutput(String stepId, String path) {
        return new CoordinationBinding(BindingSource.STEP_OUTPUT, stepId + "." + path, null);
    }

    public static CoordinationBinding executionContext(String path) {
        return new CoordinationBinding(BindingSource.EXECUTION_CONTEXT, path, null);
    }
}
