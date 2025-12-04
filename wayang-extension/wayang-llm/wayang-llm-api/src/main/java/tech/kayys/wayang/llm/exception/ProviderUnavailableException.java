package tech.kayys.wayang.models.api.exception;

/**
 * Thrown when model provider is unavailable.
 */
public class ProviderUnavailableException extends ModelException {
    
    public ProviderUnavailableException(String provider) {
        super("PROVIDER_UNAVAILABLE", "Provider unavailable: " + provider);
    }
    
    public ProviderUnavailableException(String provider, Throwable cause) {
        super("PROVIDER_UNAVAILABLE", "Provider unavailable: " + provider, cause);
    }
}