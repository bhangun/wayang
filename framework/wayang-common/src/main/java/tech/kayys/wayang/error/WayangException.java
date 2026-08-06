package tech.kayys.wayang.error;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wayang Exception - Base exception for all Wayang errors.
 * 
 * <p>This exception wraps an {@link ErrorCode} and provides structured
 * error handling throughout the system.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * try {
 *     // Some operation
 * } catch (Exception e) {
 *     throw new WayangException(
 *         ErrorCode.PLUGIN_LOAD_FAILED,
 *         "Failed to load plugin: " + pluginId,
 *         e
 *     );
 * }
 * }
 * </pre>
 */
public class WayangException extends Exception {
    
    private final ErrorCode errorCode;
    private final Map<String, Object> context = new LinkedHashMap<>();
    
    public WayangException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public WayangException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public WayangException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    public WayangException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the error code.
     *
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the error context.
     *
     * @return The error context
     */
    public Map<String, Object> getContext() {
        return context;
    }
    
    /**
     * Adds context to the error.
     *
     * @param key The context key
     * @param value The context value
     * @return This exception for chaining
     */
    public WayangException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
    
    /**
     * Adds multiple context entries.
     *
     * @param context The context map
     * @return This exception for chaining
     */
    public WayangException withContext(Map<String, Object> context) {
        this.context.putAll(context);
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s (Context: %s)", 
            errorCode.getCode(), 
            getMessage(), 
            context.isEmpty() ? "None" : context);
    }
}