package tech.kayys.wayang.exception;

/**
 * HTTP 409 Conflict Exception.
 * 
 * Thrown when a request conflicts with current state of the server.
 * 
 * @since 1.0.0
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
