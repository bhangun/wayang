package tech.kayys.wayang.exception;

/**
 * Generic Not Found Exception.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
