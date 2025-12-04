package tech.kayys.wayang.models.api.exception;

/**
 * Thrown when requested model is not found in registry.
 */
public class ModelNotFoundException extends ModelException {
    
    public ModelNotFoundException(String modelId) {
        super("MODEL_NOT_FOUND", "Model not found: " + modelId);
    }
}