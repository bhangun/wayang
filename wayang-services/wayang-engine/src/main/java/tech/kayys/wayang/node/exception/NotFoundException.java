package tech.kayys.wayang.node.exception;

/**
 * Exception for not found resources.
 */
class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
