package tech.kayys.wayang.node.exception;

/**
 * Exception for conflicting operations.
 */
class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}