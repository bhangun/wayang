package tech.kayys.wayang.context.api.exception;

/** Thrown when a source file cannot be parsed for skeletonization. */
public class SkeletonizationException extends RuntimeException {
    public SkeletonizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
