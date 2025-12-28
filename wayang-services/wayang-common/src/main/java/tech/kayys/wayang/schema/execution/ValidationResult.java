package tech.kayys.wayang.schema.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {
    private List<ValidationError> errors = new ArrayList<>();
    private List<ValidationWarning> warnings = new ArrayList<>();

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<ValidationWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public void addError(String message) {
        ValidationError error = new ValidationError();
        error.message = message;
        errors.add(error);
    }

    public void addError(ValidationError error) {
        errors.add(error);
    }

    public void addWarning(String message) {
        ValidationWarning warning = new ValidationWarning();
        warning.message = message;
        warnings.add(warning);
    }

    public void addWarning(ValidationWarning warning) {
        warnings.add(warning);
    }

    public void failure(String name, List<String> errorMessages) {
        for (String msg : errorMessages) {
            addError(msg);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!errors.isEmpty()) {
            sb.append("Errors:\n");
            for (ValidationError error : errors) {
                sb.append("  - ").append(error.message).append("\n");
            }
        }
        if (!warnings.isEmpty()) {
            sb.append("Warnings:\n");
            for (ValidationWarning warning : warnings) {
                sb.append("  - ").append(warning.message).append("\n");
            }
        }
        return sb.toString();
    }

    public static class ValidationError {
        public String code;
        public String message;
        public String nodeId;
        public String path;
        public ErrorSeverity severity = ErrorSeverity.ERROR;
        
        @Override
        public String toString() {
            return message;
        }

        public enum ErrorSeverity {
            ERROR, WARNING, INFO
        }
    }

    public static class ValidationWarning {
        public String message;
        public String nodeId;
        public String suggestion;
        
        @Override
        public String toString() {
            return message;
        }
    }
}
