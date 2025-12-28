package tech.kayys.wayang.node.dto;

/**
 * Error response wrapper for consistent error handling.
 */
public class ErrorResponse {
    private String error;
    private String message;
    private String code;
    private long timestamp;

    public ErrorResponse(String error, String message, String code) {
        this.error = error;
        this.message = message;
        this.code = code;
        this.timestamp = System.currentTimeMillis();
    }

    public static ErrorResponse from(Throwable th) {
        return new ErrorResponse(
                th.getClass().getSimpleName(),
                th.getMessage(),
                "INTERNAL_ERROR");
    }

    public static ErrorResponse notFound(String message) {
        return new ErrorResponse("NotFoundException", message, "NOT_FOUND");
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse("ConflictException", message, "CONFLICT");
    }

    public static ErrorResponse timeout(String message) {
        return new ErrorResponse("TimeoutException", message, "TIMEOUT");
    }

    // Getters
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public long getTimestamp() {
        return timestamp;
    }
}