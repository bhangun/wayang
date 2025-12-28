package tech.kayys.wayang.sdk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * SDK DTO for Validation Response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResponse {

    private boolean valid;
    private List<ValidationError> errors;
    private List<ValidationError> warnings;

    public ValidationResponse() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public static class ValidationError {
        private String field;
        private String message;
        private String code;
        private String nodeId;

        public ValidationError() {
        }

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        // Getters and setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }
    }

    // Getters and setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public List<ValidationError> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<ValidationError> warnings) {
        this.warnings = warnings;
    }
}
