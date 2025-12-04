package tech.kayys.wayang.models.api.exception;

import lombok.Getter;

/**
 * Base exception for all model-related errors.
 */
@Getter
public class ModelException extends RuntimeException {
    
    private final String errorCode;
    private final transient Object details;
    
    public ModelException(String message) {
        super(message);
        this.errorCode = "MODEL_ERROR";
        this.details = null;
    }
    
    public ModelException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public ModelException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public ModelException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public ModelException(String errorCode, String message, Throwable cause, Object details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}