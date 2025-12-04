package tech.kayys.wayang.models.api.exception;

/**
 * Thrown when model inference fails.
 */
public class ModelInferenceException extends ModelException {
    
    public ModelInferenceException(String message) {
        super("INFERENCE_FAILED", message);
    }
    
    public ModelInferenceException(String message, Throwable cause) {
        super("INFERENCE_FAILED", message, cause);
    }
    
    public ModelInferenceException(String message, Object details) {
        super("INFERENCE_FAILED", message, details);
    }
}