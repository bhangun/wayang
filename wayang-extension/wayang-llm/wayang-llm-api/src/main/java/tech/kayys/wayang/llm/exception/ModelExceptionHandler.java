package tech.kayys.wayang.models.api.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.exception.ModelException;
import tech.kayys.wayang.models.api.exception.ModelInferenceException;
import tech.kayys.wayang.models.api.exception.ModelNotFoundException;
import tech.kayys.wayang.models.api.exception.ProviderUnavailableException;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for model-related exceptions.
 */
@Provider
@Slf4j
public class ModelExceptionHandler implements ExceptionMapper<ModelException> {
    
    @Override
    public Response toResponse(ModelException exception) {
        log.error("Model exception: {}", exception.getMessage(), exception);
        
        int status = determineStatus(exception);
        
        ErrorResponse error = ErrorResponse.builder()
            .code(exception.getErrorCode())
            .message(exception.getMessage())
            .timestamp(Instant.now())
            .details(exception.getDetails())
            .build();
        
        return Response.status(status)
            .entity(error)
            .build();
    }
    
    private int determineStatus(ModelException exception) {
        if (exception instanceof ModelNotFoundException) {
            return 404;
        } else if (exception instanceof ProviderUnavailableException) {
            return 503;
        } else if (exception instanceof ModelInferenceException) {
            return 500;
        }
        return 500;
    }
    
    @lombok.Builder
    @lombok.Value
    public static class ErrorResponse {
        String code;
        String message;
        Instant timestamp;
        Object details;
    }
}